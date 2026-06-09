package show.log.reader.data.repository

import kotlinx.coroutines.flow.Flow
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.domain.model.Article

interface ArticleRepository {

    fun observeAll(): Flow<List<Article>>

    fun observeAllWithFeedTitle(): Flow<List<ArticleWithFeed>>

    fun observeUnread(): Flow<List<Article>>

    fun observeByFeed(feedId: Long): Flow<List<Article>>

    fun search(query: String): Flow<List<Article>>

    fun searchWithFeedTitle(query: String): Flow<List<ArticleWithFeed>>

    fun observeWithFeedById(id: Long): Flow<ArticleWithFeed?>

    suspend fun markAsRead(id: Long)

    suspend fun markAsUnread(id: Long)

    suspend fun markAllAsReadByFeed(feedId: Long)

    suspend fun toggleBookmark(id: Long, bookmarked: Boolean)

    fun observeIsFavorite(articleId: Long): Flow<Boolean>

    fun observeBookmarkedWithFeedTitle(): Flow<List<ArticleWithFeed>>
}
