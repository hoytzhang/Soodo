package show.log.reader.data.parser

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale

class RssParser : FeedParser {

    private val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
    )

    override fun canParse(xml: String): Boolean {
        val trimmed = xml.trimStart()
        return trimmed.contains("<rss", ignoreCase = true) ||
                trimmed.contains("<channel>", ignoreCase = true)
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
        var insideItem = false

        var articleTitle = ""
        var articleLink = ""
        var articleDescription: String? = null
        var articleContent: String? = null
        var articleAuthor: String? = null
        var articleImageUrl: String? = null
        var articlePubDate: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name.lowercase(Locale.ROOT)
                    Log.d("RSS_PARSE", "[RssParser] START_TAG <$tagName> insideItem=$insideItem")
                    when (tagName) {
                        "item" -> {
                            insideItem = true
                            articleTitle = ""
                            articleLink = ""
                            articleDescription = null
                            articleContent = null
                            articleAuthor = null
                            articleImageUrl = null
                            articlePubDate = null
                            Log.d("RSS_PARSE", "[RssParser] >>> ENTER ITEM")
                        }
                        "title" -> {
                            val text = readText(parser)
                            if (!insideItem) {
                                feedTitle = text
                                Log.d("RSS_PARSE", "[RssParser] feedTitle=\"$feedTitle\"")
                            } else {
                                articleTitle = text
                                Log.d("RSS_PARSE", "[RssParser] articleTitle=\"$articleTitle\"")
                            }
                        }
                        "link" -> {
                            if (!insideItem) {
                                feedSiteUrl = readText(parser).ifBlank { null }
                                Log.d("RSS_PARSE", "[RssParser] feedSiteUrl=$feedSiteUrl")
                            } else {
                                articleLink = readLink(parser)
                                Log.d("RSS_PARSE", "[RssParser] articleLink=\"$articleLink\"")
                            }
                        }
                        "description" -> {
                            if (!insideItem) {
                                feedDescription = readText(parser).ifBlank { null }
                            } else {
                                articleDescription = readText(parser).ifBlank { null }
                            }
                        }
                        "content", "content:encoded" -> {
                            if (insideItem) {
                                articleContent = readText(parser).ifBlank { null }
                            }
                        }
                        "creator", "dc:creator", "author" -> {
                            if (insideItem) {
                                articleAuthor = readText(parser).ifBlank { null }
                            }
                        }
                        "pubdate", "pub:date", "published" -> {
                            if (insideItem) {
                                articlePubDate = readText(parser).ifBlank { null }
                            }
                        }
                        "enclosure" -> {
                            if (insideItem) {
                                val url = parser.getAttributeValue(null, "url")
                                val type = parser.getAttributeValue(null, "type")
                                if (url != null && type?.startsWith("image/") == true) {
                                    articleImageUrl = url
                                }
                            }
                        }
                        "image" -> {
                            if (!insideItem) {
                                feedIconUrl = parseRssImage(parser)
                            }
                        }
                        "url" -> {
                            if (!insideItem && feedIconUrl == null) {
                                feedIconUrl = readText(parser).ifBlank { null }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("item", ignoreCase = true) && insideItem) {
                        insideItem = false
                        Log.d("RSS_PARSE", "[RssParser] <<< EXIT ITEM title=\"$articleTitle\" link=\"$articleLink\"")
                        if (articleTitle.isNotBlank() || articleLink.isNotBlank()) {
                            val imageUrl = articleImageUrl
                                ?: extractImageUrl(articleContent)
                                ?: extractImageUrl(articleDescription)
                            articles.add(
                                ParsedArticle(
                                    title = articleTitle.trim(),
                                    link = articleLink.trim(),
                                    author = articleAuthor?.trim(),
                                    description = stripHtml(articleDescription),
                                    content = articleContent ?: articleDescription,
                                    imageUrl = imageUrl,
                                    publishedAt = parseDate(articlePubDate),
                                )
                            )
                            Log.d("RSS_PARSE", "[RssParser] Added article #${articles.size}")
                        } else {
                            Log.d("RSS_PARSE", "[RssParser] Skipped item: title blank and link blank")
                        }
                    }
                }
            }
            event = parser.next()
        }

        Log.d("RSS_PARSE", "[RssParser] DONE: feedTitle=\"$feedTitle\" totalArticles=${articles.size}")
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

    private fun readLink(parser: XmlPullParser): String {
        val href = parser.getAttributeValue(null, "href")
        if (!href.isNullOrBlank()) return href
        return readText(parser)
    }

    private fun parseRssImage(parser: XmlPullParser): String? {
        val imageDepth = parser.depth
        var url: String? = null

        while (parser.next() != XmlPullParser.END_DOCUMENT) {

            if (
                parser.eventType == XmlPullParser.END_TAG &&
                parser.depth == imageDepth &&
                parser.name.equals("image", ignoreCase = true)
            ) {
                break
            }

            if (
                parser.eventType == XmlPullParser.START_TAG &&
                parser.name.equals("url", ignoreCase = true)
            ) {
                url = readText(parser).ifBlank { null }
            }
        }

        return url
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        for (pattern in dateFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                return sdf.parse(dateStr.trim())?.time ?: continue
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
