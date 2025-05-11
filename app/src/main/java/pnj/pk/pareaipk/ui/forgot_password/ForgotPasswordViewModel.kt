package pnj.pk.pareaipk.ui.forgot_password

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(email)
            } else {
                onFailure(task.exception?.message ?: "Unknown error")
            }
        }
    }
}
