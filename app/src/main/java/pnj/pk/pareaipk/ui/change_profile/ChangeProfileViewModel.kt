package pnj.pk.pareaipk.ui.change_profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.repository.UserRepository

class ChangeProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val TAG = "ChangeProfileViewModel"

    // LiveData untuk profil pengguna
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    // LiveData untuk status update
    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> = _updateStatus

    // LiveData untuk status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                userRepository.getCurrentUserProfile()
                    .collect { profile ->
                        _userProfile.value = profile
                        _isLoading.value = false
                        Log.d(TAG, "User profile loaded: $profile")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadUserProfile: ${e.message}", e)
                _updateStatus.value = "Terjadi kesalahan: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, profileImageUri: Uri?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Updating profile with name: $name, image URI: $profileImageUri")
                userRepository.updateUserProfile(name, profileImageUri)
                _updateStatus.value = "Profil berhasil diperbarui"
                // Reload the profile after update
                loadUserProfile()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile: ${e.message}", e)
                _updateStatus.value = "Gagal memperbarui profil: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getCurrentUserEmail(): String? {
        return userRepository.getCurrentUserEmail()
    }
}