package pnj.pk.pareaipk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.ui.login.LoginActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var keepSplashOnScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen SEBELUM super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Set content view untuk Android versi lama
        setContentView(R.layout.activity_splash_screen)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Keep splash screen visible
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // Delay sebelum navigasi
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplashOnScreen = false
            checkUserAndNavigate()
        }, 2000) // 2 detik delay
    }

    private fun checkUserAndNavigate() {
        val currentUser = auth.currentUser
        val intent = if (currentUser != null) {
            // Jika sudah login, arahkan ke MainActivity dengan flag ke Home
            Intent(this, MainActivity::class.java).apply {
                putExtra("navigateToHome", true)
                // Clear task stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            // Jika belum login, arahkan ke LoginActivity
            Intent(this, LoginActivity::class.java).apply {
                // Clear task stack
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        startActivity(intent)
        finish()
    }
}