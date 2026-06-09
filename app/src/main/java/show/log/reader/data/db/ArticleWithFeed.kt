package show.log.reader.data.db

data class ArticleWithFeed(
    val id: Long,
    val feed_id: Long,
    val title: String,
    val link: String,
    val author: String?,
    val description: String?,
    val content: String?,
    val image_url: String?,
    val published_at: Long,
    val is_read: Boolean,
    val is_bookmarked: Boolean,
    val created_at: Long,
    val updated_at: Long,
    val feed_title: String,
)
