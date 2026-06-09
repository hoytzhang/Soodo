package show.log.reader.ui.articledetail

import android.graphics.Color
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

object ArticleHtmlBuilder {

    fun buildHtml(
        title: String,
        feedTitle: String,
        date: String,
        author: String?,
        content: String?,
        imageUrl: String?,
        isDarkMode: Boolean,
    ): String {
        val bgColor = if (isDarkMode) "#000000" else "#ffffff"
        val textColor = if (isDarkMode) "#ffffff" else "#000000"
        val secondaryColor = if (isDarkMode) "#b0b0b0" else "#666666"
        val dividerColor = if (isDarkMode) "#333333" else "#e0e0e0"

        val imageBlock = if (imageUrl != null) """
            <div class="cover-image">
                <img src="$imageUrl" alt="cover" />
            </div>
        """.trimIndent() else ""

        val authorBlock = if (!author.isNullOrBlank()) """
            <span class="meta-separator">&middot;</span>
            <span class="meta-author">$author</span>
        """.trimIndent() else ""

        val bodyContent = if (!content.isNullOrBlank()) content else "<p>No content available.</p>"

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0" />
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    font-size: 16px;
                    line-height: 1.7;
                    color: $textColor;
                    background-color: $bgColor;
                    padding: 16px;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                .cover-image {
                    margin: 0 -16px 16px -16px;
                }
                .cover-image img {
                    width: 100%;
                    max-height: 280px;
                    object-fit: cover;
                    display: block;
                }
                .title {
                    font-size: 22px;
                    font-weight: 700;
                    line-height: 1.35;
                    margin-bottom: 12px;
                    color: $textColor;
                }
                .meta {
                    font-size: 13px;
                    color: $secondaryColor;
                    margin-bottom: 16px;
                }
                .meta-feed {
                    font-weight: 500;
                }
                .meta-separator {
                    margin: 0 4px;
                }
                .divider {
                    border: none;
                    border-top: 1px solid $dividerColor;
                    margin: 0 0 16px 0;
                }
                .content img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                    margin: 12px 0;
                }
                .content p {
                    margin-bottom: 14px;
                }
                .content a {
                    color: ${if (isDarkMode) "#64b5f6" else "#1976d2"};
                    text-decoration: none;
                }
                .content h1, .content h2, .content h3, .content h4 {
                    margin: 20px 0 10px 0;
                    font-weight: 600;
                    line-height: 1.4;
                }
                .content ul, .content ol {
                    margin: 10px 0 14px 24px;
                }
                .content blockquote {
                    border-left: 3px solid $secondaryColor;
                    padding-left: 12px;
                    margin: 14px 0;
                    color: $secondaryColor;
                    font-style: italic;
                }
                .content pre {
                    background-color: ${if (isDarkMode) "#1a1a1a" else "#f5f5f5"};
                    padding: 12px;
                    border-radius: 4px;
                    overflow-x: auto;
                    font-size: 14px;
                    margin: 12px 0;
                }
                .content iframe {
                    max-width: 100%;
                }
            </style>
        </head>
        <body>
            $imageBlock
            <h1 class="title">${escapeHtml(title)}</h1>
            <div class="meta">
                <span class="meta-feed">${escapeHtml(feedTitle)}</span>
                <span class="meta-separator">&middot;</span>
                <span class="meta-date">$date</span>
                $authorBlock
            </div>
            <hr class="divider" />
            <div class="content">
                $bodyContent
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun configureWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            blockNetworkImage = false
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            textZoom = 100
        }
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.webViewClient = WebViewClient()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
