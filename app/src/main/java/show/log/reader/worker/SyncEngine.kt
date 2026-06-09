package show.log.reader.worker

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import show.log.reader.data.db.dao.ArticleDao
import show.log.reader.data.db.dao.FeedDao
import show.log.reader.data.db.entity.FeedEntity
import show.log.reader.data.mapper.toArticleEntity
import show.log.reader.data.network.RssApiService
import show.log.reader.data.parser.FeedParserFactory
import show.log.reader.data.parser.ParsedFeed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val rssApiService: RssApiService,
    private val feedParserFactory: FeedParserFactory,
) {

    private val semaphore = Semaphore(CONCURRENCY)

    suspend fun syncFeed(feedEntity: FeedEntity): SyncResult {
        return try {
            val cachedEtag = feedEntity.etag
            val cachedLastModified = feedEntity.lastModified
            val hasCacheHeaders = cachedEtag != null || cachedLastModified != null

            Log.d("RSS_FETCH", "[syncFeed] feedId=${feedEntity.id} url=${feedEntity.url} hasCacheHeaders=$hasCacheHeaders")
            val response = rssApiService.fetchFeed(
                url = feedEntity.url,
                etag = if (hasCacheHeaders) cachedEtag else null,
                lastModified = if (hasCacheHeaders) cachedLastModified else null,
            )
            Log.d("RSS_FETCH", "[syncFeed] feedId=${feedEntity.id} code=${response.code()}")

            if (response.code() == 304) {
                val articleCount = articleDao.countByFeedId(feedEntity.id)
                Log.d("RSS_FETCH", "[syncFeed] feedId=${feedEntity.id} 304 Not Modified, localArticles=$articleCount")
                if (articleCount == 0) {
                    Log.d("RSS_FETCH", "[syncFeed] feedId=${feedEntity.id} localArticles=0, force re-fetch without cache")
                    val forcedResponse = rssApiService.fetchFeed(
                        url = feedEntity.url,
                    )
                    Log.d("RSS_FETCH", "[syncFeed] feedId=${feedEntity.id} force code=${forcedResponse.code()}")
                    if (!forcedResponse.isSuccessful) {
                        return SyncResult(
                            feedId = feedEntity.id,
                            feedUrl = feedEntity.url,
                            error = IllegalStateException("HTTP ${forcedResponse.code()}"),
                        )
                    }
                    val forcedBody = forcedResponse.body()
                        ?: return SyncResult(
                            feedId = feedEntity.id,
                            feedUrl = feedEntity.url,
                            error = IllegalStateException("Empty response"),
                        )
                    Log.d("RSS_PARSE", "[syncFeed] feedId=${feedEntity.id} force parsing body (${forcedBody.length} chars)")
                    val forcedParsed = feedParserFactory.parse(forcedBody)
                    Log.d("RSS_PARSE", "[syncFeed] feedId=${feedEntity.id} force parsed articleCount=${forcedParsed.articles.size}")
                    updateFeedMetadata(feedEntity, forcedParsed)
                    val newCount = saveArticles(feedEntity.id, forcedParsed)
                    feedDao.updateSyncState(
                        id = feedEntity.id,
                        syncTime = System.currentTimeMillis(),
                        etag = forcedResponse.headers()["ETag"],
                        lastModified = forcedResponse.headers()["Last-Modified"],
                    )
                    Log.d("RSS_SAVE", "[syncFeed] feedId=${feedEntity.id} force savedArticles=$newCount")
                    return SyncResult(
                        feedId = feedEntity.id,
                        feedUrl = feedEntity.url,
                        newArticles = newCount,
                    )
                }

                feedDao.updateSyncState(
                    id = feedEntity.id,
                    syncTime = System.currentTimeMillis(),
                    etag = cachedEtag,
                    lastModified = cachedLastModified,
                )
                Log.d("RSS_SAVE", "[syncFeed] feedId=${feedEntity.id} skipped (localArticles=$articleCount)")
                return SyncResult(
                    feedId = feedEntity.id,
                    feedUrl = feedEntity.url,
                    skipped = true,
                )
            }

            if (!response.isSuccessful) {
                Log.d("RSS_FETCH", "[syncFeed] feedId=${feedEntity.id} error code=${response.code()}")
                return SyncResult(
                    feedId = feedEntity.id,
                    feedUrl = feedEntity.url,
                    error = IllegalStateException("HTTP ${response.code()}"),
                )
            }

            val body = response.body()
                ?: return SyncResult(
                    feedId = feedEntity.id,
                    feedUrl = feedEntity.url,
                    error = IllegalStateException("Empty response"),
                )

            Log.d("RSS_PARSE", "[syncFeed] feedId=${feedEntity.id} parsing body (${body.length} chars)")
            val parsed = feedParserFactory.parse(body)
            Log.d("RSS_PARSE", "[syncFeed] feedId=${feedEntity.id} parsed title=\"${parsed.title}\" articleCount=${parsed.articles.size}")

            updateFeedMetadata(feedEntity, parsed)

            val newCount = saveArticles(feedEntity.id, parsed)

            feedDao.updateSyncState(
                id = feedEntity.id,
                syncTime = System.currentTimeMillis(),
                etag = response.headers()["ETag"],
                lastModified = response.headers()["Last-Modified"],
            )

            Log.d("RSS_SAVE", "[syncFeed] feedId=${feedEntity.id} newArticles=$newCount")
            SyncResult(
                feedId = feedEntity.id,
                feedUrl = feedEntity.url,
                newArticles = newCount,
            )
        } catch (e: Exception) {
            SyncResult(
                feedId = feedEntity.id,
                feedUrl = feedEntity.url,
                error = e,
            )
        }
    }

    suspend fun syncAll(): List<SyncResult> = coroutineScope {
        val feeds = feedDao.getAllFeeds()
        feeds.map { feed ->
            async {
                semaphore.withPermit {
                    syncFeed(feed)
                }
            }
        }.awaitAll()
    }

    private suspend fun updateFeedMetadata(feedEntity: FeedEntity, parsed: ParsedFeed) {
        val updated = feedEntity.copy(
            title = parsed.title.ifBlank { feedEntity.title },
            description = parsed.description ?: feedEntity.description,
            siteUrl = parsed.siteUrl ?: feedEntity.siteUrl,
            iconUrl = parsed.iconUrl ?: feedEntity.iconUrl,
        )
        feedDao.update(updated)
    }

    private suspend fun saveArticles(feedId: Long, parsed: ParsedFeed): Int {
        if (parsed.articles.isEmpty()) return 0

        val existingLinks = articleDao.getLinksByFeedId(feedId)

        val newArticles = parsed.articles
            .filter { it.link !in existingLinks }
            .map { it.toArticleEntity(feedId) }

        if (newArticles.isEmpty()) return 0

        articleDao.insertAll(newArticles)
        articleDao.rebuildFts()
        return newArticles.size
    }

    companion object {
        private const val CONCURRENCY = 4
    }
}
