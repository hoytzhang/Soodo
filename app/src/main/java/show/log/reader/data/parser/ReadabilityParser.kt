package show.log.reader.data.parser

import net.dankito.readability4j.Readability4J
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

data class ReadabilityResult(
    val title: String,
    val content: String,
    val excerpt: String?,
    val byline: String?,
)

@Singleton
class ReadabilityParser @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    suspend fun parse(url: String): ReadabilityResult? {
        return try {
            val html = fetchHtml(url) ?: return null
            val readability = Readability4J(url, html)
            val article = readability.parse()

            val content = article.content
            if (content.isNullOrBlank() || content.length < MIN_CONTENT_LENGTH) {
                return null
            }

            ReadabilityResult(
                title = article.title ?: "",
                content = content,
                excerpt = article.excerpt,
                byline = article.byline,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchHtml(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return null
            }
            val body = response.body?.string()
            response.close()
            body
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val MIN_CONTENT_LENGTH = 100
    }
}
