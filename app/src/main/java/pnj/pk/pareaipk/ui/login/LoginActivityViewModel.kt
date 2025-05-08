package pnj.pk.pareaipk.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // LiveData untuk user
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    // LiveData untuk error
    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> get() = _loginError

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser
                            _user.postValue(currentUser)
                            _loginError.postValue(null)
                        } else {
                            _user.postValue(null)
                            _loginError.postValue("Email atau password salah.") // <<< custom error
                            Log.w("LoginViewModel", "signInWithEmail:failure", task.exception)
                        }
                    }
            } catch (e: Exception) {
                _loginError.postValue("Terjadi kesalahan: ${e.localizedMessage}")
                Log.e("LoginViewModel", "Error during sign-in", e)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)

        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser
                            _user.postValue(currentUser)
                            _loginError.postValue(null)
                        } else {
                            _user.postValue(null)
                            _loginError.postValue("Google Sign-In gagal.") // <<< custom error
                            Log.w("LoginViewModel", "signInWithCredential:failure", task.exception)
                        }
                    }
            } catch (e: Exception) {
                _loginError.postValue("Terjadi kesalahan: ${e.localizedMessage}")
                Log.e("LoginViewModel", "Error during Google sign-in", e)
            }
        }
    }

    fun checkCurrentUser() {
        _user.value = auth.currentUser
    }
}
