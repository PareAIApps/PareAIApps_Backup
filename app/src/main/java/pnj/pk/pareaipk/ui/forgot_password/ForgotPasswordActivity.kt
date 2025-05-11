package pnj.pk.pareaipk.ui.forgot_password

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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

        supportActionBar?.hide()

        binding.sendResetLinkButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            forgotPasswordViewModel.sendPasswordResetEmail(
                email = email,
                onSuccess = { emailAddress ->
                    binding.progressBar.visibility = View.GONE
                    val message = getString(R.string.reset_link_sent, emailAddress)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    showConfirmationDialog()
                },
                onFailure = { errorMessage ->
                    binding.progressBar.visibility = View.GONE
                    val message = getString(R.string.reset_link_failed, errorMessage)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.email_reset_title))
            .setMessage(getString(R.string.email_reset_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
