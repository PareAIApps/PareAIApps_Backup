package pnj.pk.pareaipk.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
            hideProgressBar() // Hide progress bar when login completes
            if (user != null) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Log.w("LoginActivity", "Login failed or user not logged in.")
            }
        })

        // Observe error LiveData
        loginViewModel.loginError.observe(this, Observer { errorMessage ->
            hideProgressBar() // Hide progress bar when error occurs
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                showErrorDialog(it)  // Menampilkan dialog error
            }
        })

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                showProgressBar() // Show progress bar for email login
                loginViewModel.signInWithEmail(email, password)
            } else {
                Toast.makeText(this, getString(R.string.email_password_empty), Toast.LENGTH_SHORT).show()
            }
        }

        binding.signInButton.setOnClickListener {
            showProgressBar() // Show progress bar when Google Sign-In starts
            signInWithGoogle()
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        binding.forgotPasswordText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }
    }

    private fun showProgressBar() {
        // Show loading overlay with semi-transparent background
        binding.loadingOverlay.visibility = View.VISIBLE
        // Progress bar is inside the overlay, so it will be visible automatically
    }

    private fun hideProgressBar() {
        // Hide loading overlay
        binding.loadingOverlay.visibility = View.GONE
        // All interactions are automatically restored when overlay is hidden
    }

    private fun showErrorDialog(message: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.internet_connection_warning_title))
        builder.setMessage(getString(R.string.internet_connection_warning_message))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()

        // Set the OK button color to green_light
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green_light))
    }

    private fun showInternetStabilityWarning() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.login_failed_title))
        builder.setMessage(getString(R.string.google_account_selection_message))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false) // Prevent dismissing by clicking outside

        val dialog = builder.create()
        dialog.show()

        // Set the OK button color to green_light
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green_light))
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
                hideProgressBar() // Hide progress bar on error
                Log.d("LoginActivity", "Google Sign-In Error: ${e.message}")
                // Show internet stability warning when Google Sign-In fails
                showInternetStabilityWarning()
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
                        hideProgressBar() // Hide progress bar on parsing error
                        Log.e("LoginActivity", "Invalid Google ID token", e)
                        Toast.makeText(this, getString(R.string.invalid_google_token), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    hideProgressBar() // Hide progress bar on unexpected credential type
                    Log.e("LoginActivity", "Unexpected type of credential")
                    Toast.makeText(this, getString(R.string.unexpected_credential_type), Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                hideProgressBar() // Hide progress bar on unexpected credential
                Log.e("LoginActivity", "Unexpected type of credential")
                Toast.makeText(this, getString(R.string.unexpected_credential_type), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loginViewModel.checkCurrentUser()
    }
}