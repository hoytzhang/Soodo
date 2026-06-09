package show.log.reader.data.repository.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import show.log.reader.data.db.dao.ArticleDao
import show.log.reader.data.db.dao.FeedDao
import show.log.reader.data.mapper.toArticleEntity
import show.log.reader.data.mapper.toDomain
import show.log.reader.data.mapper.toEntity
import show.log.reader.data.mapper.toFeedEntity
import show.log.reader.data.network.RssApiService
import show.log.reader.data.parser.FeedParserFactory
import show.log.reader.data.repository.FeedRepository
import show.log.reader.domain.model.Feed
import show.log.reader.worker.SyncEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val rssApiService: RssApiService,
    private val feedParserFactory: FeedParserFactory,
    private val syncEngine: SyncEngine,
) : FeedRepository {

    override suspend fun addFeed(url: String): Result<Feed> = runCatching {
        withContext(Dispatchers.IO) {
            val existing = feedDao.getByUrl(url)
            if (existing != null) {
                throw IllegalArgumentException("Feed already exists: $url")
            }

            Log.d("RSS_FETCH", "[addFeed] Requesting $url")
            val response = rssApiService.fetchFeed(url)
            val body = response.body()
                ?: throw IllegalStateException("Empty response from $url")
            Log.d("RSS_FETCH", "[addFeed] Response code=${response.code()} bodyLength=${body.length}")

            Log.d("RSS_PARSE", "[addFeed] Parsing body (${body.length} chars)...")
            val parsed = feedParserFactory.parse(body)
            Log.d("RSS_PARSE", "[addFeed] Parsed feed title=\"${parsed.title}\" articleCount=${parsed.articles.size}")
            val entity = parsed.toFeedEntity(url)
            val id = feedDao.insert(entity)

            var articleCount = 0
            if (parsed.articles.isNotEmpty()) {
                val existingLinks = articleDao.getLinksByFeedId(id).toSet()
                val newArticles = parsed.articles
                    .filter { it.link !in existingLinks }
                    .map { it.toArticleEntity(id) }
                if (newArticles.isNotEmpty()) {
                    articleDao.insertAll(newArticles)
                    articleDao.rebuildFts()
                    articleCount = newArticles.size
                }
            }

            feedDao.updateSyncState(
                id = id,
                syncTime = System.currentTimeMillis(),
                etag = if (articleCount > 0) response.headers()["ETag"] else null,
                lastModified = if (articleCount > 0) response.headers()["Last-Modified"] else null,
            )

            val savedFeed = feedDao.getById(id)!!.toDomain()
            Log.d("RSS_SAVE", "[addFeed] feedId=$id title=\"${savedFeed.title}\" savedArticles=$articleCount etag=${response.headers()["ETag"]}")
            savedFeed
        }
    }

    override suspend fun deleteFeed(id: Long): Result<Unit> = runCatching {
        feedDao.deleteById(id)
    }

    override suspend fun updateFeed(feed: Feed): Result<Unit> = runCatching {
        val existing = feedDao.getById(feed.id)
            ?: throw IllegalArgumentException("Feed not found: ${feed.id}")
        feedDao.update(feed.toEntity())
    }

    override fun observeAll(): Flow<List<Feed>> {
        return feedDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): Feed? {
        return feedDao.getById(id)?.toDomain()
    }

    override suspend fun getByUrl(url: String): Feed? {
        return feedDao.getByUrl(url)?.toDomain()
    }

    override suspend fun refreshFeed(id: Long): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val entity = feedDao.getById(id)
                ?: throw IllegalArgumentException("Feed not found: $id")

            val result = syncEngine.syncFeed(entity)
            if (result.error != null) {
                throw result.error
            }
        }
    }

    override suspend fun refreshAll(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val results = syncEngine.syncAll()
            val failed = results.filter { it.error != null }
            if (failed.isNotEmpty()) {
                throw SyncException(failed.map { it.feedUrl to it.error!! })
            }
        }
    }
}

class SyncException(val errors: List<Pair<String, Throwable>>) : Exception(
    "Sync failed for ${errors.size} feed(s): ${errors.joinToString { it.first }}"
)
