package show.log.reader.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import show.log.reader.data.db.entity.FeedEntity

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feed: FeedEntity): Long

    @Update
    suspend fun update(feed: FeedEntity)

    @Delete
    suspend fun delete(feed: FeedEntity)

    @Query("DELETE FROM feed WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM feed WHERE id = :id")
    suspend fun getById(id: Long): FeedEntity?

    @Query("SELECT * FROM feed WHERE url = :url")
    suspend fun getByUrl(url: String): FeedEntity?

    @Query("SELECT * FROM feed ORDER BY title ASC")
    fun getAll(): Flow<List<FeedEntity>>

    @Query("SELECT COUNT(*) FROM feed")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM feed WHERE last_sync_at = 0 OR last_sync_at <= :threshold ORDER BY last_sync_at ASC LIMIT :limit")
    suspend fun getStaleFeeds(threshold: Long, limit: Int = 20): List<FeedEntity>

    @Query("SELECT * FROM feed ORDER BY last_sync_at ASC")
    suspend fun getAllFeeds(): List<FeedEntity>

    @Query("UPDATE feed SET last_sync_at = :syncTime, etag = :etag, last_modified = :lastModified, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSyncState(id: Long, syncTime: Long, etag: String?, lastModified: String?, updatedAt: Long = System.currentTimeMillis())
}
