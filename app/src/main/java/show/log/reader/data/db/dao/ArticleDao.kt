package show.log.reader.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import show.log.reader.data.db.ArticleWithFeed
import show.log.reader.data.db.entity.ArticleEntity

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(article: ArticleEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<ArticleEntity>): List<Long>

    @Update
    suspend fun update(article: ArticleEntity)

    @Query("SELECT * FROM article WHERE id = :id")
    suspend fun getById(id: Long): ArticleEntity?

    @Query("SELECT * FROM article WHERE id = :id")
    fun observeById(id: Long): Flow<ArticleEntity?>

    @Query("""
        SELECT a.*, f.title AS feed_title 
        FROM article a 
        INNER JOIN feed f ON a.feed_id = f.id 
        WHERE a.id = :id
    """)
    fun observeWithFeedById(id: Long): Flow<ArticleWithFeed?>

    @Query("SELECT * FROM article WHERE feed_id = :feedId ORDER BY published_at DESC")
    fun getByFeedId(feedId: Long): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM article ORDER BY published_at DESC")
    fun getAll(): Flow<List<ArticleEntity>>

    @Query("""
        SELECT a.*, f.title AS feed_title 
        FROM article a 
        INNER JOIN feed f ON a.feed_id = f.id 
        ORDER BY a.published_at DESC
    """)
    fun getAllWithFeedTitle(): Flow<List<ArticleWithFeed>>

    @Query("""
        SELECT a.*, f.title AS feed_title 
        FROM article a 
        INNER JOIN feed f ON a.feed_id = f.id 
        WHERE a.is_read = 0 
        ORDER BY a.published_at DESC
    """)
    fun getUnreadWithFeedTitle(): Flow<List<ArticleWithFeed>>

    @Query("SELECT * FROM article WHERE is_read = 0 ORDER BY published_at DESC")
    fun getUnread(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM article WHERE is_bookmarked = 1 ORDER BY published_at DESC")
    fun getBookmarked(): Flow<List<ArticleEntity>>

    @Query("""
        SELECT a.*, f.title AS feed_title 
        FROM article a 
        INNER JOIN feed f ON a.feed_id = f.id 
        WHERE a.is_bookmarked = 1 
        ORDER BY a.published_at DESC
    """)
    fun getBookmarkedWithFeedTitle(): Flow<List<ArticleWithFeed>>

    @Query("SELECT * FROM article WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY published_at DESC")
    fun search(query: String): Flow<List<ArticleEntity>>

    @Query("""
        SELECT a.*, f.title AS feed_title 
        FROM article a 
        INNER JOIN feed f ON a.feed_id = f.id 
        WHERE a.id IN (
            SELECT rowid FROM article_fts WHERE article_fts MATCH :query
        )
        ORDER BY a.published_at DESC
        LIMIT 100
    """)
    fun searchWithFeedTitle(query: String): Flow<List<ArticleWithFeed>>

    @Query("INSERT INTO article_fts(article_fts) VALUES('rebuild')")
    suspend fun rebuildFts()

    @Query("UPDATE article SET is_read = :isRead, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE article SET is_read = :isRead, updated_at = :updatedAt WHERE feed_id = :feedId")
    suspend fun updateReadStatusByFeed(feedId: Long, isRead: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE article SET is_bookmarked = :isBookmarked, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateBookmarkStatus(id: Long, isBookmarked: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM article WHERE feed_id = :feedId AND is_read = 0")
    fun getUnreadCountByFeed(feedId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM article WHERE is_read = 0")
    fun getTotalUnreadCount(): Flow<Int>

    @Query("DELETE FROM article WHERE feed_id = :feedId")
    suspend fun deleteByFeedId(feedId: Long)

    @Query("DELETE FROM article WHERE is_read = 1 AND is_bookmarked = 0 AND published_at < :threshold")
    suspend fun deleteOldReadArticles(threshold: Long): Int

    @Query("SELECT link FROM article WHERE feed_id = :feedId")
    suspend fun getLinksByFeedId(feedId: Long): List<String>

    @Query("SELECT COUNT(*) FROM article WHERE feed_id = :feedId")
    suspend fun countByFeedId(feedId: Long): Int
}
