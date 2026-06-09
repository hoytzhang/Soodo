package show.log.reader.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import show.log.reader.data.db.dao.ArticleDao
import show.log.reader.data.db.dao.FavoriteDao
import show.log.reader.data.db.dao.FeedDao
import show.log.reader.data.db.entity.ArticleEntity
import show.log.reader.data.db.entity.ArticleFtsEntity
import show.log.reader.data.db.entity.FavoriteEntity
import show.log.reader.data.db.entity.FeedEntity

@Database(
    entities = [
        FeedEntity::class,
        ArticleEntity::class,
        FavoriteEntity::class,
        ArticleFtsEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE feed ADD COLUMN category TEXT")
            }
        }
    }
}
