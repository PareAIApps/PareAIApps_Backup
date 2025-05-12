package pnj.pk.pareaipk.ui.share_apps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.repository.UserRepository

class ShareAppsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val TAG = "ShareAppsViewModel"

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> get() = _userProfile

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // Get current user's email
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.email?.let { email ->
                    // Directly fetch user profile synchronously
                    val profile = withContext(Dispatchers.IO) {
                        userRepository.getUserProfileSync(email)
                    }

                    // Update LiveData
                    _userProfile.value = profile
                    Log.d(TAG, "Profile loaded successfully: $profile")
                } ?: run {
                    Log.e(TAG, "No current user found")
                    _userProfile.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
                _userProfile.value = null
            }
        }
    }

    // Function to refresh profile data
    fun refreshProfile() {
        loadUserProfile()
    }
}