package pnj.pk.pareaipk.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.MainActivity
import pnj.pk.pareaipk.databinding.ActivityOnboardingBinding
import pnj.pk.pareaipk.ui.login.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Periksa status autentikasi pengguna
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Pengguna sudah login, arahkan ke MainActivity
            navigateToMainActivity()
        } else {
            // Pengguna belum login, arahkan ke LoginActivity
            binding.button.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        supportActionBar?.hide()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}