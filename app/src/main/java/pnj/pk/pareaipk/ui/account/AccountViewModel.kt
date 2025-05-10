package pnj.pk.pareaipk.ui.account

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.repository.UserRepository

class AccountViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfileLiveData = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> get() = _userProfileLiveData

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUserProfile().collect { profile ->
                _userProfileLiveData.postValue(profile)
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
                userRepository.updateUserProfile(name, profileImageUri)
                _updateStatus.value = "Profil berhasil diperbarui"
                // Reload the profile after update
                loadUserProfile()
            } catch (e: Exception) {
                _updateStatus.value = "Gagal memperbarui profil: ${e.message}"
            }
        }
    }

    fun logout() {
        userRepository.logout()
    }

    fun getEmail(): String? = userRepository.getCurrentUserEmail()
}