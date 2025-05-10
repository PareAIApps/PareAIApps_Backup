package pnj.pk.pareaipk.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase

class RegisterActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository: UserRepository

    init {
        val database = UserRoomDatabase.getDatabase(application)
        userRepository = UserRepository(database, auth)
    }

    // LiveData untuk user yang berhasil registrasi
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    // LiveData untuk pesan error/sukses
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun registerWithEmail(email: String, password: String, confirmPassword: String, name: String = "") {
        // Validasi input
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _message.postValue("Semua field harus diisi!")
            return
        }

        if (password != confirmPassword) {
            _message.postValue("Password tidak cocok!")
            return
        }

        // Proses registrasi
        _message.postValue("Memulai registrasi...")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser

                    // Save user profile to Room database
                    viewModelScope.launch {
                        try {
                            // Create UserProfile with default name (from email) if name is empty
                            val displayName = if (name.isEmpty()) email.substringBefore("@") else name

                            val userProfile = UserProfile(
                                email = email,
                                name = displayName,
                                profileImageUri = null
                            )

                            // Insert into Room database
                            userRepository.saveUserProfile(userProfile)

                            _user.postValue(currentUser)
                        } catch (e: Exception) {
                            _message.postValue("Registrasi berhasil tapi gagal menyimpan profil: ${e.localizedMessage}")
                        }
                    }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        _message.postValue("Email sudah terdaftar.")
                    } else {
                        _message.postValue("Registrasi gagal: ${exception?.localizedMessage}")
                    }
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}