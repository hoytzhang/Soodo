package show.log.reader.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SyncPreference @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val SYNC_INTERVAL_MINUTES = longPreferencesKey("sync_interval_minutes")
        val THEME_MODE = intPreferencesKey("theme_mode")
    }

    val syncIntervalMinutes: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.SYNC_INTERVAL_MINUTES] ?: DEFAULT_INTERVAL_MINUTES
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: THEME_SYSTEM
    }

    suspend fun setSyncIntervalMinutes(minutes: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SYNC_INTERVAL_MINUTES] = minutes
        }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_MINUTES = 30L
        const val NEVER_INTERVAL = -1L
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2

        val INTERVALS = listOf(
            15L to "15 minutes",
            30L to "30 minutes",
            60L to "1 hour",
            180L to "3 hours",
            NEVER_INTERVAL to "Never",
        )

        val THEME_OPTIONS = listOf(
            THEME_SYSTEM to "Follow system",
            THEME_LIGHT to "Light",
            THEME_DARK to "Dark",
        )
    }
}
