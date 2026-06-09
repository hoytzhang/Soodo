package show.log.reader.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncEngine: SyncEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val results = syncEngine.syncAll()

        val hasErrors = results.any { it.error != null }
        val allFailed = results.isNotEmpty() && results.all { it.error != null }

        return when {
            allFailed -> Result.retry()
            hasErrors -> Result.retry()
            else -> Result.success()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "rss_sync_periodic"
    }
}
