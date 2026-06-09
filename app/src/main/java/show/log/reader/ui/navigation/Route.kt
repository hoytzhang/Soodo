package show.log.reader.ui.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Search : Route("search")
    data object Bookmarks : Route("bookmarks")
    data object Settings : Route("settings")
    data object FeedList : Route("feed_list")
    data object AddFeed : Route("add_feed")
    data object EditFeed : Route("edit_feed/{feedId}") {
        fun createRoute(feedId: Long) = "edit_feed/$feedId"
    }
    data object ArticleDetail : Route("article/{articleId}") {
        fun createRoute(articleId: Long) = "article/$articleId"
    }
}
