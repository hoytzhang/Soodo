package show.log.reader.data.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.data.db.dao.ArticleDao
import show.log.reader.data.db.dao.FavoriteDao
import show.log.reader.data.db.entity.FavoriteEntity
import show.log.reader.data.mapper.toDomain
import show.log.reader.data.repository.ArticleRepository
import show.log.reader.domain.model.Article
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val favoriteDao: FavoriteDao,
) : ArticleRepository {

    override fun observeAll(): Flow<List<Article>> {
        return articleDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllWithFeedTitle(): Flow<List<ArticleWithFeed>> {
        return articleDao.getAllWithFeedTitle()
    }

    override fun observeUnread(): Flow<List<Article>> {
        return articleDao.getUnread().map { list -> list.map { it.toDomain() } }
    }

    override fun observeByFeed(feedId: Long): Flow<List<Article>> {
        return articleDao.getByFeedId(feedId).map { list -> list.map { it.toDomain() } }
    }

    override fun search(query: String): Flow<List<Article>> {
        return articleDao.search(query).map { list -> list.map { it.toDomain() } }
    }

    override fun searchWithFeedTitle(query: String): Flow<List<ArticleWithFeed>> {
        return articleDao.searchWithFeedTitle(query)
    }

    override fun observeWithFeedById(id: Long): Flow<ArticleWithFeed?> {
        return articleDao.observeWithFeedById(id)
    }

    override suspend fun markAsRead(id: Long) {
        articleDao.updateReadStatus(id, true)
    }

    override suspend fun markAsUnread(id: Long) {
        articleDao.updateReadStatus(id, false)
    }

    override suspend fun markAllAsReadByFeed(feedId: Long) {
        articleDao.updateReadStatusByFeed(feedId, true)
    }

    override suspend fun toggleBookmark(id: Long, bookmarked: Boolean) {
        articleDao.updateBookmarkStatus(id, bookmarked)
        if (bookmarked) {
            favoriteDao.insert(FavoriteEntity(articleId = id))
        } else {
            favoriteDao.deleteByArticleId(id)
        }
    }

    override fun observeIsFavorite(articleId: Long): Flow<Boolean> {
        return favoriteDao.observeIsFavorite(articleId)
    }

    override fun observeBookmarkedWithFeedTitle(): Flow<List<ArticleWithFeed>> {
        return articleDao.getBookmarkedWithFeedTitle()
    }
}
