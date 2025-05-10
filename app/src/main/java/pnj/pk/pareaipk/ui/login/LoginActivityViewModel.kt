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

    private val TAG = "LoginViewModel"
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

                            // Check if user profile exists in Room
                            viewModelScope.launch {
                                try {
                                    val existingProfile = userRepository.getUserProfileSync(email)
                                    if (existingProfile == null) {
                                        // Only create a new profile if one doesn't exist
                                        Log.d(TAG, "No profile found for $email, creating new profile")
                                        val userProfile = UserProfile(
                                            email = email,
                                            name = email.substringBefore("@"),
                                            profileImageUri = null
                                        )
                                        userRepository.saveUserProfile(userProfile)
                                    } else {
                                        Log.d(TAG, "Using existing profile for $email: $existingProfile")
                                    }
                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error checking/creating user profile", e)
                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                }
                            }
                        } else {
                            _user.postValue(null)
                            _loginError.postValue("Email atau password salah.")
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                        }
                    }
            } catch (e: Exception) {
                _loginError.postValue("Terjadi kesalahan: ${e.localizedMessage}")
                Log.e(TAG, "Error during sign-in", e)
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

                            viewModelScope.launch {
                                try {
                                    val email = currentUser?.email ?: return@launch

                                    // Check if user profile exists in Room first
                                    val existingProfile = userRepository.getUserProfileSync(email)

                                    if (existingProfile == null) {
                                        // Only create a new profile if one doesn't exist
                                        Log.d(TAG, "No profile found for Google user $email, creating new profile")
                                        val userProfile = UserProfile(
                                            email = email,
                                            name = currentUser.displayName ?: email.substringBefore("@"),
                                            profileImageUri = currentUser.photoUrl?.toString()
                                        )
                                        userRepository.saveUserProfile(userProfile)
                                    } else {
                                        Log.d(TAG, "Using existing profile for Google user $email: $existingProfile")
                                    }

                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error checking/saving Google user profile", e)
                                    _user.postValue(currentUser)
                                    _loginError.postValue(null)
                                }
                            }
                        } else {
                            _user.postValue(null)
                            _loginError.postValue("Google Sign-In gagal.")
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                        }
                    }
            } catch (e: Exception) {
                _loginError.postValue("Terjadi kesalahan: ${e.localizedMessage}")
                Log.e(TAG, "Error during Google sign-in", e)
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
                        // Create a profile only if none exists
                        Log.d(TAG, "No profile found for current user $email, creating new profile")
                        val userProfile = UserProfile(
                            email = email,
                            name = currentUser.displayName ?: email.substringBefore("@"),
                            profileImageUri = currentUser.photoUrl?.toString()
                        )
                        userRepository.saveUserProfile(userProfile)
                    } else {
                        Log.d(TAG, "Using existing profile for current user $email: $existingProfile")
                    }

                    _user.postValue(currentUser)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in checkCurrentUser", e)
                    _user.postValue(currentUser)
                }
            }
        } else {
            _user.postValue(null)
        }
    }
}