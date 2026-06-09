package show.log.reader

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import show.log.reader.ui.articledetail.ArticleDetailScreen
import show.log.reader.ui.feeds.AddEditFeedScreen
import show.log.reader.ui.feeds.FeedListScreen
import show.log.reader.ui.home.BookmarksScreen
import show.log.reader.ui.home.HomeScreen
import show.log.reader.ui.navigation.Route
import show.log.reader.ui.search.SearchScreen
import show.log.reader.ui.settings.SettingsScreen
import show.log.reader.ui.theme.MyApplicationTheme
import show.log.reader.ui.settings.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var volumeScrollHandler: ((down: Boolean) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                RssNavGraph()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (volumeScrollHandler?.invoke(true) == true) return true
                }
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (volumeScrollHandler?.invoke(false) == true) return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (volumeScrollHandler?.invoke(true) == true) return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (volumeScrollHandler?.invoke(false) == true) return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
private fun RssNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Route.ArticleDetail.createRoute(articleId))
                },
                onSearchClick = {
                    navController.navigate(Route.Search.route)
                },
                onBookmarksClick = {
                    navController.navigate(Route.Bookmarks.route)
                },
                onSettingsClick = {
                    navController.navigate(Route.Settings.route)
                },
                onFeedsClick = {
                    navController.navigate(Route.FeedList.route)
                },
            )
        }

        composable(Route.FeedList.route) {
            FeedListScreen(
                onBack = { navController.popBackStack() },
                onAddFeed = { navController.navigate(Route.AddFeed.route) },
                onEditFeed = { feedId ->
                    navController.navigate(Route.EditFeed.createRoute(feedId))
                },
            )
        }

        composable(Route.AddFeed.route) {
            AddEditFeedScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.EditFeed.route,
            arguments = listOf(
                navArgument("feedId") { type = NavType.LongType },
            ),
        ) {
            AddEditFeedScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Route.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onArticleClick = { articleId ->
                    navController.navigate(Route.ArticleDetail.createRoute(articleId))
                },
            )
        }

        composable(Route.Bookmarks.route) {
            BookmarksScreen(
                onBack = { navController.popBackStack() },
                onArticleClick = { articleId ->
                    navController.navigate(Route.ArticleDetail.createRoute(articleId))
                },
            )
        }

        composable(Route.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.LongType },
            ),
        ) {
            ArticleDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
