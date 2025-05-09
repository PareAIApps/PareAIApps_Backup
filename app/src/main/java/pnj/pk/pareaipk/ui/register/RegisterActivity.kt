package pnj.pk.pareaipk.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
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

            // You can add a name field to your registration layout if you want,
            // or extract a username from the email as shown in the ViewModel

            // If you have a name field:
            // val name = binding.nameInputRegister.text.toString().trim()
            // registerViewModel.registerWithEmail(email, password, confirmPassword, name)

            // Otherwise:
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
}