package pnj.pk.pareaipk.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import pnj.pk.pareaipk.MainActivity
import pnj.pk.pareaipk.databinding.ActivityRegisterBinding
import pnj.pk.pareaipk.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe LiveData from ViewModel
        registerViewModel.user.observe(this, Observer { user ->
            user?.let {
                showRegistrationSuccessAlert()
            }
        })

        registerViewModel.message.observe(this, Observer { message ->
            if (message.contains("email sudah", ignoreCase = true)) {
                showAlertEmailSudahTerdaftar()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })

        // Button register
        binding.registerButton.setOnClickListener {
            val email = binding.emailInputRegister.text.toString().trim()
            val password = binding.passwordInputRegister.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            registerViewModel.registerWithEmail(email, password, confirmPassword)
        }

        // Link ke login
        binding.loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        supportActionBar?.hide()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showAlertEmailSudahTerdaftar() {
        AlertDialog.Builder(this)
            .setTitle("Registrasi Gagal")
            .setMessage("Email sudah terdaftar. Silakan gunakan email lain atau login.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showRegistrationSuccessAlert() {
        AlertDialog.Builder(this)
            .setTitle("Registrasi Berhasil")
            .setMessage("Akun Anda telah berhasil dibuat!")
            .setPositiveButton("OK") { _, _ ->
                // Navigate to MainActivity (which contains HomeFragment)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false) // Prevent dismissing by tapping outside
            .show()
    }
}