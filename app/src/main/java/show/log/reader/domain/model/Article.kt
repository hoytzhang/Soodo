package show.log.reader.domain.model

data class Article(
    val id: Long = 0,
    val feedId: Long,
    val title: String,
    val link: String,
    val author: String? = null,
    val description: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val publishedAt: Long,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
