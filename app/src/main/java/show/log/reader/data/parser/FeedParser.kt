package show.log.reader.data.parser

interface FeedParser {

    fun parse(xml: String): ParsedFeed

    fun canParse(xml: String): Boolean
}
