package pnj.pk.pareaipk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.ui.login.LoginActivity

class SplashScreenActivity : BaseActivity() {

    private val TAG = "SplashScreenActivity"
    private val splashTimeMillis: Long = 2000 // 2 detik
    private lateinit var auth: FirebaseAuth
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Apply user language first, then proceed with splash logic
        applyUserLanguageAndProceed()
    }

    private fun applyUserLanguageAndProceed() {
        activityScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser?.email != null) {
                    // User is logged in, initialize and apply their language preference
                    val database = UserRoomDatabase.getDatabase(this@SplashScreenActivity)
                    val userRepository = UserRepository(database, auth)

                    // Initialize user settings
                    userRepository.initializeUserSettings()

                    // Apply the saved language
                    applyUserLanguage()

                    Log.d(TAG, "Applied user language for logged in user: ${currentUser.email}")
                } else {
                    Log.d(TAG, "No logged in user, using system language")
                }

                // Proceed with splash screen logic after language is applied
                proceedAfterLanguageSetup()

            } catch (e: Exception) {
                Log.e(TAG, "Error applying language in splash: ${e.message}", e)
                // Proceed anyway
                proceedAfterLanguageSetup()
            }
        }
    }

    private fun proceedAfterLanguageSetup() {
        // Menunggu splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Jika sudah login, arahkan ke MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Jika belum login, arahkan ke LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            finish() // Supaya setelah pindah ke activity lain, tombol back tidak kembali ke splash
        }, splashTimeMillis)
    }
}