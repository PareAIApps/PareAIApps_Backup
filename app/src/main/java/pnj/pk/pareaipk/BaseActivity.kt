package pnj.pk.pareaipk

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import java.util.*

open class BaseActivity : AppCompatActivity() {

    private val TAG = "BaseActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "BaseActivity onCreate for ${this.javaClass.simpleName}")
    }

    override fun attachBaseContext(newBase: Context?) {
        Log.d(TAG, "attachBaseContext called for ${this.javaClass.simpleName}")
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context?): Context? {
        if (context == null) return null

        return try {
            val languageCode = getSavedLanguageSync(context)
            Log.d(TAG, "updateBaseContextLocale: applying language $languageCode")

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)

            context.createConfigurationContext(config)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateBaseContextLocale: ${e.message}", e)
            context
        }
    }

    private fun getSavedLanguageSync(context: Context): String {
        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            Log.d(TAG, "getSavedLanguageSync: currentUser = ${currentUser?.email}")

            if (currentUser?.email != null) {
                val database = UserRoomDatabase.getDatabase(context)
                val userRepository = UserRepository(database, auth)

                // Gunakan runBlocking untuk operasi sinkron
                val userSettings = runBlocking {
                    try {
                        userRepository.initializeUserSettings()
                        userRepository.getUserSettingsSync(currentUser.email!!)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting user settings: ${e.message}", e)
                        null
                    }
                }

                val languageCode = userSettings?.languageCode ?: 0
                val languageString = if (languageCode == 1) "id" else "en"

                Log.d(TAG, "Found saved language: $languageString (code: $languageCode) for ${currentUser.email}")
                return languageString
            } else {
                Log.d(TAG, "No logged in user, using system language")
                return getSystemLanguage(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting saved language: ${e.message}", e)
            return getSystemLanguage(context)
        }
    }

    private fun getSystemLanguage(context: Context): String {
        return try {
            val systemLanguage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0].language
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale.language
            }
            if (systemLanguage == "id" || systemLanguage == "in") "id" else "en"
        } catch (e: Exception) {
            Log.w(TAG, "Error getting system language, defaulting to 'en'", e)
            "en"
        }
    }

    protected fun applyUserLanguage() {
        try {
            val languageCode = getSavedLanguageSync(this)
            setAppLocale(languageCode)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying user language: ${e.message}", e)
        }
    }

    private fun setAppLocale(languageCode: String) {
        try {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(resources.configuration)
            config.setLocale(locale)

            resources.updateConfiguration(config, resources.displayMetrics)

            Log.d(TAG, "App locale set to: $languageCode in ${this.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting app locale: ${e.message}", e)
        }
    }
}