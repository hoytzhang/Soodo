package show.log.reader.data.parser

data class ParsedArticle(
    val title: String,
    val link: String,
    val author: String?,
    val description: String?,
    val content: String?,
    val imageUrl: String?,
    val publishedAt: Long,
)
