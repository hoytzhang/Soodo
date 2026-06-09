package show.log.reader.data.db.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = ArticleEntity::class)
@Entity(tableName = "article_fts")
data class ArticleFtsEntity(
    val title: String,
    val description: String?,
    val content: String?,
)
