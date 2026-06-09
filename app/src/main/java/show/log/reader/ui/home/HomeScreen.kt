package show.log.reader.ui.home

import android.util.Log
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
fun HomeScreen(
    onArticleClick: (articleId: Long) -> Unit,
    onSearchClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFeedsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
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
                title = { Text("素读") },
                actions = {
                    IconButton(onClick = onFeedsClick) {
                        Icon(painter = painterResource(R.drawable.notifications), contentDescription = "Feeds", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { viewModel.refresh() }, enabled = !isRefreshing) {
                        Icon(painter = painterResource(R.drawable.refresh), contentDescription = "Refresh", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onBookmarksClick) {
                        Icon(painter = painterResource(R.drawable.favorite), contentDescription = "Bookmarks", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(painter = painterResource(R.drawable.search), contentDescription = "Search", modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(painter = painterResource(R.drawable.settings), contentDescription = "Settings", modifier = Modifier.size(24.dp))
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
                        itemAnimator = null
                        overScrollMode = android.view.View.OVER_SCROLL_NEVER
                        addItemDecoration(
                            HorizontalDividerItemDecoration(ctx),
                        )
                    }.also { recyclerView = it }
                },
                update = { rv ->
                    val a = rv.adapter as? ArticleListAdapter
                    a?.isDarkMode = isDarkMode
                    a?.submitList(articles)
                    Log.d("RSS_UI", "[HomeScreen] submitList size=${articles.size}")
                },
            )
        }
    }
}

internal class HorizontalDividerItemDecoration(
    context: android.content.Context,
) : RecyclerView.ItemDecoration() {

    private val dividerHeight = (1 * context.resources.displayMetrics.density).toInt()

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: android.view.View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        outRect.bottom = dividerHeight
    }
}
