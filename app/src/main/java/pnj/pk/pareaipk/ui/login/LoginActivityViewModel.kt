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
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase

class LoginActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository: UserRepository

    init {
        val database = UserRoomDatabase.getDatabase(application)
        userRepository = UserRepository(database, auth)
    }

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

                            // Verify if user profile exists in Room, if not create it
                            viewModelScope.launch {
                                try {
                                    val existingProfile = userRepository.getUserProfileSync(email)
                                    if (existingProfile == null) {
                                        // Create a basic profile if none exists
                                        val userProfile = UserProfile(
                                            email = email,
                                            name = email.substringBefore("@"),
                                            profileImageUri = null
                                        )
                                        userRepository.saveUserProfile(userProfile)
                                    }
                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                } catch (e: Exception) {
                                    Log.e("LoginViewModel", "Error checking/creating user profile", e)
                                    // Still set the user, as authentication succeeded
                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                }
                            }
                        } else {
                            _user.postValue(null)
                            _loginError.postValue("Email atau password salah.")
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

                            // For Google sign-in, create or update the user profile in Room
                            viewModelScope.launch {
                                try {
                                    val email = currentUser?.email ?: return@launch

                                    // Use Firebase user display name, email, and photo URL
                                    val userProfile = UserProfile(
                                        email = email,
                                        name = currentUser.displayName ?: email.substringBefore("@"),
                                        profileImageUri = currentUser.photoUrl?.toString()
                                    )

                                    // Save to Room database
                                    userRepository.saveUserProfile(userProfile)

                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                } catch (e: Exception) {
                                    Log.e("LoginViewModel", "Error saving Google user profile", e)
                                    // Still set the user, as authentication succeeded
                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                }
                            }
                        } else {
                            _user.postValue(null)
                            _loginError.postValue("Google Sign-In gagal.")
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
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val email = currentUser.email ?: return@launch
                    val existingProfile = userRepository.getUserProfileSync(email)

                    if (existingProfile == null) {
                        // Create a profile if none exists for currently logged in user
                        val userProfile = UserProfile(
                            email = email,
                            name = currentUser.displayName ?: email.substringBefore("@"),
                            profileImageUri = currentUser.photoUrl?.toString()
                        )
                        userRepository.saveUserProfile(userProfile)
                    }

                    _user.postValue(currentUser)
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Error in checkCurrentUser", e)
                    _user.postValue(currentUser)
                }
            }
        } else {
            _user.postValue(null)
        }
    }
}