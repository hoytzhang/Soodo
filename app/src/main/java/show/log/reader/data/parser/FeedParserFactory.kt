package show.log.reader.data.parser

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedParserFactory @Inject constructor() {

    private val parsers: List<FeedParser> = listOf(
        RssParser(),
        AtomParser(),
    )

    fun parse(xml: String): ParsedFeed {
        val parser = parsers.firstOrNull { it.canParse(xml) }
            ?: throw IllegalArgumentException("Unsupported feed format")
        Log.d("RSS_PARSE", "[FeedParserFactory] Using parser=${parser::class.simpleName}")
        val result = parser.parse(xml)
        Log.d("RSS_PARSE", "[FeedParserFactory] Result title=\"${result.title}\" articleCount=${result.articles.size}")
        return result
    }
}
