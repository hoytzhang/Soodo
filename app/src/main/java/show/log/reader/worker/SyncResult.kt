package show.log.reader.worker

data class SyncResult(
    val feedId: Long,
    val feedUrl: String,
    val newArticles: Int = 0,
    val skipped: Boolean = false,
    val error: Throwable? = null,
)
