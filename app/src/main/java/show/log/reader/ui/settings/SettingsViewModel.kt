package show.log.reader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import show.log.reader.util.SyncPreference
import show.log.reader.worker.SyncScheduler
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncPreference: SyncPreference,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    val syncIntervalMinutes: StateFlow<Long> = syncPreference.syncIntervalMinutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SyncPreference.DEFAULT_INTERVAL_MINUTES,
        )

    val themeMode: StateFlow<Int> = syncPreference.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SyncPreference.THEME_SYSTEM,
        )

    fun setSyncInterval(minutes: Long) {
        viewModelScope.launch {
            syncPreference.setSyncIntervalMinutes(minutes)
            if (minutes == SyncPreference.NEVER_INTERVAL) {
                syncScheduler.cancelPeriodicSync()
            } else {
                syncScheduler.schedulePeriodicSync(minutes)
            }
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            syncPreference.setThemeMode(mode)
        }
    }
}
