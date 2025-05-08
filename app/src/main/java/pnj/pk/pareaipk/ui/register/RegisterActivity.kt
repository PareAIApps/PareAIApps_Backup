package pnj.pk.pareaipk.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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
                // Registration successful
                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // Finish RegisterActivity to avoid stacking
            }
        })

        // Observe message LiveData
        registerViewModel.message.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        // Register Button Click Listener
        binding.registerButton.setOnClickListener {
            val email = binding.emailInputRegister.text.toString().trim()
            val password = binding.passwordInputRegister.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            registerViewModel.registerWithEmail(email, password, confirmPassword)
        }

        // Login Link
        binding.loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Finish RegisterActivity to avoid stacking
        }

        supportActionBar?.hide()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Ensure no stacked activity remains
    }
}
