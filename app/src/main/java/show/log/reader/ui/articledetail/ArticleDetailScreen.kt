package show.log.reader.ui.articledetail

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import show.log.reader.MainActivity
import show.log.reader.R
import show.log.reader.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBack: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel(),
) {
    val article by viewModel.article.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isReadingMode by viewModel.isReadingMode.collectAsStateWithLifecycle()
    val readingContent by viewModel.readingContent.collectAsStateWithLifecycle()
    val isLoadingReadingMode by viewModel.isLoadingReadingMode.collectAsStateWithLifecycle()
    val isDarkMode = LocalIsDarkTheme.current

    var currentWebView by remember { mutableStateOf<WebView?>(null) }
    val activity = LocalContext.current as MainActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activity.volumeScrollHandler = { down ->
                        val wv = currentWebView
                        if (wv == null) {
                            false
                        } else {
                            val distance = (wv.height * 1f).toInt()
                            if (down) wv.scrollBy(0, distance) else wv.scrollBy(0, -distance)
                            true
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    activity.volumeScrollHandler = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            activity.volumeScrollHandler = null
        }
    }

    var showFlash by remember { mutableStateOf(false) }

    LaunchedEffect(isLoadingReadingMode) {
        if (isLoadingReadingMode) {
            showFlash = true
        }
    }

    LaunchedEffect(showFlash) {
        if (showFlash) {
            delay(500)
            showFlash = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = article?.feed_title ?: "",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(R.drawable.arrowback), contentDescription = "Back", modifier = Modifier.size(24.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleReadingMode() }, enabled = !isLoadingReadingMode) {
                        Icon(painter = painterResource(R.drawable.chromereadermode), contentDescription = "Reading mode", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { viewModel.refresh() }, enabled = !isRefreshing) {
                        Icon(painter = painterResource(R.drawable.refresh), contentDescription = "Refresh", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(painter = painterResource(R.drawable.favorite), contentDescription = if (isBookmarked) "Unfavorite" else "Favorite", modifier = Modifier.size(24.dp))
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (article == null) {
                androidx.compose.material3.LinearProgressIndicator(modifier = Modifier.fillMaxSize())
            } else if (isReadingMode && readingContent != null) {
                ReadingModeWebView(
                    title = readingContent!!.title,
                    feedTitle = article!!.feed_title,
                    content = readingContent!!.content,
                    author = readingContent!!.byline ?: article!!.author,
                    imageUrl = article!!.image_url,
                    isDarkMode = isDarkMode,
                    onWebViewReady = { currentWebView = it },
                )
            } else {
                ArticleUrlWebView(
                    url = article!!.link,
                    onWebViewReady = { currentWebView = it },
                )
            }

            if (showFlash) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkMode) Color.Black else Color.White),
                )
            }
        }
    }
}

@Composable
private fun ArticleUrlWebView(
    url: String,
    onWebViewReady: (WebView) -> Unit = {},
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
            webView?.destroy()
            webView = null
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).also { wv ->
                ArticleHtmlBuilder.configureWebView(wv)
                webView = wv
                onWebViewReady(wv)
            }
        },
        update = { wv ->
            wv.loadUrl(url)
        },
    )
}

@Composable
private fun ReadingModeWebView(
    title: String,
    feedTitle: String,
    content: String,
    author: String?,
    imageUrl: String?,
    isDarkMode: Boolean,
    onWebViewReady: (WebView) -> Unit = {},
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
            webView?.destroy()
            webView = null
        }
    }

    val html = ArticleHtmlBuilder.buildHtml(
        title = title,
        feedTitle = feedTitle,
        date = "",
        author = author,
        content = content,
        imageUrl = imageUrl,
        isDarkMode = isDarkMode,
    )

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).also { wv ->
                ArticleHtmlBuilder.configureWebView(wv)
                webView = wv
                onWebViewReady(wv)
            }
        },
        update = { wv ->
            wv.loadDataWithBaseURL(
                null,
                html,
                "text/html",
                "UTF-8",
                null,
            )
        },
    )
}
