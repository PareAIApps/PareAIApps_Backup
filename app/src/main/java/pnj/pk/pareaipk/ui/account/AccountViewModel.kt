package pnj.pk.pareaipk.ui.account

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.repository.UserRepository

class AccountViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val TAG = "AccountViewModel"

    private val _userProfileLiveData = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> get() = _userProfileLiveData

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUserProfile().collect { profile ->
                    Log.d(TAG, "Received profile from flow: $profile")
                    _userProfileLiveData.postValue(profile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
            }
        }
    }

    // Function to refresh profile data
    fun refreshProfile() {
        loadUserProfile()
    }

    fun updateProfile(name: String, profileImageUri: Uri? = null) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating profile with name: $name, image URI: $profileImageUri")
                userRepository.updateUserProfile(name, profileImageUri)
                _updateStatus.value = "Profil berhasil diperbarui"
                // Reload the profile after update
                loadUserProfile()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                _updateStatus.value = "Gagal memperbarui profil: ${e.message}"
            }
        }
    }

    fun logout() {
        // We just call logout in the repository, which now only signs out from Firebase Auth
        Log.d(TAG, "Logging out user")
        userRepository.logout()
    }

    fun getEmail(): String? = userRepository.getCurrentUserEmail()
}