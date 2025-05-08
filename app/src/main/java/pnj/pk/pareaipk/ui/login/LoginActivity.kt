package pnj.pk.pareaipk.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.ActivityLoginBinding
import pnj.pk.pareaipk.MainActivity
import pnj.pk.pareaipk.ui.forgot_password.ForgotPasswordActivity
import pnj.pk.pareaipk.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe user LiveData
        loginViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Log.w("LoginActivity", "Login failed or user not logged in.")
            }
        })

        // Observe error LiveData
        loginViewModel.loginError.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                showErrorDialog(it)  // Menampilkan dialog error
            }
        })

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.signInWithEmail(email, password)
            } else {
                Toast.makeText(this, "Email dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        binding.forgotPasswordText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }
    }

    private fun showErrorDialog(message: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Login Gagal")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun signInWithGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                handleGoogleSignIn(result)
            } catch (e: Exception) {
                Log.d("LoginActivity", "Google Sign-In Error: ${e.message}")
            }
        }
    }

    private fun handleGoogleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        loginViewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("LoginActivity", "Invalid Google ID token", e)
                    }
                } else {
                    Log.e("LoginActivity", "Unexpected type of credential")
                }
            }
            else -> {
                Log.e("LoginActivity", "Unexpected type of credential")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loginViewModel.checkCurrentUser()
    }
}
