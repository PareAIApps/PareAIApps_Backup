package pnj.pk.pareaipk.ui.forgot_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _resetStatus = MutableLiveData<String>()
    val resetStatus: LiveData<String> get() = _resetStatus

    fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty()) {
            _resetStatus.value = "Harap masukkan email Anda."
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _resetStatus.value = "Tautan reset kata sandi telah dikirim ke $email"
            } else {
                _resetStatus.value = "Gagal mengirim tautan: ${task.exception?.message}"
            }
        }
    }
}