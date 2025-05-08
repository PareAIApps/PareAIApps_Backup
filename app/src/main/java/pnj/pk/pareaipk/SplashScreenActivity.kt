package pnj.pk.pareaipk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.ui.login.LoginActivity
import pnj.pk.pareaipk.MainActivity

class SplashScreenActivity : AppCompatActivity() {

    private val splashTimeMillis: Long = 2000 // 2 detik
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen) // HARUS pakai activity_splash_screen.xml

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
