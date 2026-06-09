package show.log.reader.data.parser

data class ParsedFeed(
    val title: String,
    val description: String?,
    val siteUrl: String?,
    val iconUrl: String?,
    val articles: List<ParsedArticle>,
)
