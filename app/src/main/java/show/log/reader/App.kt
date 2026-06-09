package show.log.reader

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import show.log.reader.util.SyncPreference
import show.log.reader.worker.SyncScheduler
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var syncScheduler: SyncScheduler

    @Inject lateinit var syncPreference: SyncPreference

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleInitialSync()
    }

    private fun scheduleInitialSync() {
        CoroutineScope(Dispatchers.IO).launch {
            val interval = syncPreference.syncIntervalMinutes.first()
            if (interval == SyncPreference.NEVER_INTERVAL) {
                syncScheduler.cancelPeriodicSync()
            } else {
                syncScheduler.schedulePeriodicSync(interval)
            }
        }
    }
}
