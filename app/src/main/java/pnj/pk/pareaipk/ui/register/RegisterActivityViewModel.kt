package pnj.pk.pareaipk.ui.register

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData untuk status registrasi
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    // LiveData untuk menampilkan pesan
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // Menangani registrasi dengan email dan password
    fun registerWithEmail(email: String, password: String, confirmPassword: String) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _message.postValue("Semua field harus diisi!")
            return
        }

        if (password != confirmPassword) {
            _message.postValue("Password tidak cocok!")
            return
        }

        // Start registration
        _message.postValue("Memulai registrasi...")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    _user.postValue(currentUser) // Update LiveData
                } else {
                    _message.postValue("Registrasi gagal: ${task.exception?.message}")
                }
            }
    }

    // Untuk logout jika perlu
    fun logout() {
        auth.signOut()
    }
}
