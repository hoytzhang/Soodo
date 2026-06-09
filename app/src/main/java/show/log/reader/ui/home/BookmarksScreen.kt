package show.log.reader.ui.home

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import show.log.reader.MainActivity
import show.log.reader.R
import show.log.reader.ui.common.adapter.ArticleListAdapter
import show.log.reader.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onArticleClick: (articleId: Long) -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val isDarkMode = LocalIsDarkTheme.current

    var recyclerView by remember { mutableStateOf<RecyclerView?>(null) }
    val activity = LocalContext.current as MainActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activity.volumeScrollHandler = { down ->
                        val rv = recyclerView
                        if (rv == null) {
                            false
                        } else {
                            val distance = (rv.height * 1f).toInt()
                            if (down) rv.scrollBy(0, distance) else rv.scrollBy(0, -distance)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(R.drawable.arrowback), contentDescription = "返回", modifier = Modifier.size(24.dp))
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
            if (articles.isEmpty()) {
                Text(
                    text = "No bookmarks yet",
                    modifier = Modifier.align(Alignment.Center),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                var adapter by remember { mutableStateOf<ArticleListAdapter?>(null) }

                if (adapter == null) {
                    adapter = ArticleListAdapter(
                        onItemClick = { article ->
                            onArticleClick(article.id)
                        },
                    )
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        RecyclerView(ctx).apply {
                            layoutManager = LinearLayoutManager(ctx)
                            this.adapter = adapter
                            addItemDecoration(
                                HorizontalDividerItemDecoration(ctx),
                            )
                        }.also { recyclerView = it }
                    },
                    update = { rv ->
                        val a = rv.adapter as? ArticleListAdapter
                        a?.isDarkMode = isDarkMode
                        a?.submitList(articles)
                    },
                )
            }
        }
    }
}
