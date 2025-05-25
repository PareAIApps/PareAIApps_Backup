package pnj.pk.pareaipk.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    private val NOTIFICATION_KEY = booleanPreferencesKey("notification_enabled")
    private val LANGUAGE_KEY = stringPreferencesKey("language_key")

    // Get the current notification setting as a Flow
    val isNotificationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_KEY] ?: true // Default to true
        }

    // Update the notification setting
    suspend fun setNotificationEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_KEY] = isEnabled
        }
    }

    // Get the current language setting as a Flow
    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            val savedLanguage = preferences[LANGUAGE_KEY]
            if (savedLanguage.isNullOrEmpty()) {
                // Jika belum ada bahasa tersimpan, kembalikan string kosong
                // Biarkan fragment yang menentukan default berdasarkan sistem
                ""
            } else {
                savedLanguage
            }
        }

    // Update the language setting
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    // Method untuk mendapatkan bahasa dengan fallback ke sistem
    fun getLanguageWithSystemFallback(): Flow<String> = context.dataStore.data
        .map { preferences ->
            val savedLanguage = preferences[LANGUAGE_KEY]
            if (savedLanguage.isNullOrEmpty()) {
                // Jika belum ada yang tersimpan, gunakan bahasa sistem
                val systemLanguage = context.resources.configuration.locales[0].language
                if (systemLanguage == "id" || systemLanguage == "in") "id" else "en"
            } else {
                savedLanguage
            }
        }

    companion object {
        @Volatile
        private var INSTANCE: SettingsPreferences? = null

        fun getInstance(context: Context): SettingsPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsPreferences(context)
                INSTANCE = instance
                instance
            }
        }
    }
}