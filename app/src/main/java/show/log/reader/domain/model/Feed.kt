package show.log.reader.domain.model

data class Feed(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val url: String,
    val siteUrl: String? = null,
    val iconUrl: String? = null,
    val lastSyncAt: Long = 0,
    val etag: String? = null,
    val lastModified: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String? = null,
)
