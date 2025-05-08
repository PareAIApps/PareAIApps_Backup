package pnj.pk.pareaipk.ui.forgot_password

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observing reset status from ViewModel
        forgotPasswordViewModel.resetStatus.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            if (message.contains("Tautan reset")) {
                // Show confirmation dialog after sending the reset link
                showConfirmationDialog()
            }
        }

        // Send Reset Link button click listener
        binding.sendResetLinkButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            // Show progress bar when sending reset email
            binding.progressBar.visibility = android.view.View.VISIBLE

            // Send reset password request through ViewModel
            forgotPasswordViewModel.sendPasswordResetEmail(email)

            // Hide progress bar after operation is completed
            binding.progressBar.visibility = android.view.View.GONE
        }

        supportActionBar?.hide()
    }

    private fun showConfirmationDialog() {
        // Create AlertDialog to confirm email reset request
        AlertDialog.Builder(this)
            .setTitle("Email Reset Sent")
            .setMessage("A password reset link has been sent to your email. Please check your inbox.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()  // Close ForgotPasswordActivity and go back to login screen
            }
            .setCancelable(false)
            .show()
    }
}
