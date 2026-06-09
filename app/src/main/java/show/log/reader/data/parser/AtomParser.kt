package show.log.reader.data.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale

class AtomParser : FeedParser {

    private val dateFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
    )

    override fun canParse(xml: String): Boolean {
        val trimmed = xml.trimStart()
        return trimmed.contains("<feed", ignoreCase = true) &&
                trimmed.contains("xmlns", ignoreCase = true)
    }

    override fun parse(xml: String): ParsedFeed {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var feedTitle = ""
        var feedDescription: String? = null
        var feedSiteUrl: String? = null
        var feedIconUrl: String? = null
        val articles = mutableListOf<ParsedArticle>()

        var event = parser.eventType
        var insideEntry = false

        var articleTitle = ""
        var articleLink = ""
        var articleDescription: String? = null
        var articleContent: String? = null
        var articleAuthor: String? = null
        var articleImageUrl: String? = null
        var articleUpdated: String? = null
        var articlePublished: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "entry" -> {
                            insideEntry = true
                            articleTitle = ""
                            articleLink = ""
                            articleDescription = null
                            articleContent = null
                            articleAuthor = null
                            articleImageUrl = null
                            articleUpdated = null
                            articlePublished = null
                        }
                        "title" -> {
                            if (!insideEntry) {
                                feedTitle = readText(parser)
                            } else {
                                articleTitle = readText(parser)
                            }
                        }
                        "subtitle" -> {
                            if (!insideEntry) {
                                feedDescription = readText(parser).ifBlank { null }
                            }
                        }
                        "link" -> {
                            val href = parser.getAttributeValue(null, "href")
                            val rel = parser.getAttributeValue(null, "rel")
                            if (!insideEntry) {
                                if (href.isNullOrBlank()) {
                                    feedSiteUrl = href
                                }
                                if (rel == "alternate" || rel == null) {
                                    feedSiteUrl = href
                                }
                            } else {
                                when (rel) {
                                    "enclosure" -> {
                                        val type = parser.getAttributeValue(null, "type")
                                        if (type?.startsWith("image/") == true) {
                                            articleImageUrl = href
                                        }
                                    }
                                    "alternate", null -> {
                                        if (articleLink.isBlank() && !href.isNullOrBlank()) {
                                            articleLink = href
                                        }
                                    }
                                }
                            }
                        }
                        "summary" -> {
                            if (insideEntry) {
                                articleDescription = readText(parser).ifBlank { null }
                            }
                        }
                        "content" -> {
                            if (insideEntry) {
                                articleContent = readText(parser).ifBlank { null }
                            }
                        }
                        "name", "author" -> {
                            if (insideEntry) {
                                if (parser.name == "name") {
                                    articleAuthor = readText(parser).ifBlank { null }
                                } else {
                                    articleAuthor = parseAtomAuthor(parser) ?: articleAuthor
                                }
                            }
                        }
                        "published", "issued" -> {
                            if (insideEntry) {
                                articlePublished = readText(parser).ifBlank { null }
                            }
                        }
                        "updated", "modified" -> {
                            if (insideEntry) {
                                articleUpdated = readText(parser).ifBlank { null }
                            }
                        }
                        "icon" -> {
                            if (!insideEntry) {
                                feedIconUrl = readText(parser).ifBlank { null }
                            }
                        }
                        "logo" -> {
                            if (!insideEntry && feedIconUrl.isNullOrBlank()) {
                                feedIconUrl = readText(parser).ifBlank { null }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "entry" && insideEntry) {
                        insideEntry = false
                        if (articleTitle.isNotBlank() || articleLink.isNotBlank()) {
                            val imageUrl = articleImageUrl
                                ?: extractImageUrl(articleContent)
                                ?: extractImageUrl(articleDescription)
                            articles.add(
                                ParsedArticle(
                                    title = articleTitle.trim(),
                                    link = articleLink.trim(),
                                    author = articleAuthor?.trim(),
                                    description = stripHtml(articleDescription ?: articleContent),
                                    content = articleContent ?: articleDescription,
                                    imageUrl = imageUrl,
                                    publishedAt = parseDate(articlePublished ?: articleUpdated),
                                )
                            )
                        }
                    }
                }
            }
            event = parser.next()
        }

        return ParsedFeed(
            title = feedTitle.trim(),
            description = feedDescription?.trim(),
            siteUrl = feedSiteUrl?.trim(),
            iconUrl = feedIconUrl?.trim(),
            articles = articles,
        )
    }

    private fun readText(parser: XmlPullParser): String {
        val sb = StringBuilder()
        var event = parser.next()
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.TEXT || event == XmlPullParser.CDSECT) {
                sb.append(parser.text)
            }
            event = parser.next()
        }
        return sb.toString()
    }

    private fun parseAtomAuthor(parser: XmlPullParser): String? {
        var name: String? = null
        var depth = 1
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT && depth > 0) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "name" && name == null) {
                        name = readText(parser).ifBlank { null }
                    }
                    depth++
                }
                XmlPullParser.END_TAG -> depth--
            }
            if (depth > 0) event = parser.next()
        }
        return name
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val cleaned = dateStr.trim()
        for (pattern in dateFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                return sdf.parse(cleaned)?.time ?: continue
            } catch (_: Exception) {
                continue
            }
        }
        return System.currentTimeMillis()
    }

    private fun stripHtml(html: String?): String? {
        if (html.isNullOrBlank()) return null
        return html.replace(Regex("<[^>]+>"), "").trim().ifBlank { null }
    }

    private fun extractImageUrl(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val imgRegex = Regex("""<img[^>]+src\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        return imgRegex.find(html)?.groupValues?.getOrNull(1)
    }
}
