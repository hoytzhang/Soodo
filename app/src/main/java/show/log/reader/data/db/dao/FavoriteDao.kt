package show.log.reader.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import show.log.reader.data.db.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity): Long

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE article_id = :articleId")
    suspend fun deleteByArticleId(articleId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE article_id = :articleId)")
    suspend fun isFavorite(articleId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE article_id = :articleId)")
    fun observeIsFavorite(articleId: Long): Flow<Boolean>

    @Query("SELECT * FROM favorite ORDER BY created_at DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT COUNT(*) FROM favorite")
    fun getCount(): Flow<Int>
}
