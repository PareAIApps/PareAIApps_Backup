package pnj.pk.pareaipk.ui.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.utils.Event

/**
 * ViewModel for the Settings screen that handles all settings-related logic
 */
class SettingsViewModel(
    private val repository: UserRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val TAG = "SettingsViewModel"

    // Notification state as LiveData
    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled: LiveData<Boolean> = _notificationEnabled

    // User status
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // For navigation events
    private val _navigationEvent = MutableLiveData<Event<NavigationDestination>>()
    val navigationEvent: LiveData<Event<NavigationDestination>> = _navigationEvent

    // For toast or snackbar messages
    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    // Get notification settings as Flow
    val notificationEnabledFlow: Flow<Boolean> = settingsPreferences.isNotificationEnabled

    init {
        checkLoginStatus()
    }

    /**
     * Updates notification preference in DataStore
     */
    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsPreferences.setNotificationEnabled(enabled)
                _notificationEnabled.postValue(enabled)

                // Schedule or cancel notifications based on setting
                if (enabled) {
                    // You might want to inject context here or use work manager differently
                    _message.postValue(Event("Notifikasi diaktifkan"))
                } else {
                    _message.postValue(Event("Notifikasi dinonaktifkan"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification settings: ${e.message}", e)
                _message.postValue(Event("Gagal mengubah pengaturan notifikasi"))
            }
        }
    }

    /**
     * Load current notification setting
     */
    fun loadNotificationSetting() {
        viewModelScope.launch {
            try {
                // This is collected once when needed
                settingsPreferences.isNotificationEnabled.collect { isEnabled ->
                    _notificationEnabled.postValue(isEnabled)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notification settings: ${e.message}", e)
            }
        }
    }

    /**
     * Check if user is currently logged in
     */
    private fun checkLoginStatus() {
        val currentUser = repository.getCurrentUser()
        _isLoggedIn.value = currentUser != null
    }

    /**
     * Handle logout action
     */
    fun logout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Logging out user")
                repository.logout()
                _isLoggedIn.postValue(false)
                _navigationEvent.postValue(Event(NavigationDestination.LOGIN))
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout: ${e.message}", e)
                _message.postValue(Event("Gagal keluar dari akun"))
            }
        }
    }

    /**
     * Navigate to account settings
     */
    fun navigateToAccount() {
        _navigationEvent.postValue(Event(NavigationDestination.ACCOUNT))
    }

    /**
     * Navigate to about screen
     */
    fun navigateToAbout() {
        _navigationEvent.postValue(Event(NavigationDestination.ABOUT))
    }

    /**
     * Share the app with others
     */
    fun shareApp(context: Context) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,
                "Coba aplikasi PARE AI untuk mendeteksi hama padi! Download sekarang: [DOWNLOAD_LINK]")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan aplikasi via"))
    }

    // Navigation destinations
    enum class NavigationDestination {
        LOGIN, ACCOUNT, ABOUT
    }
}

/**
 * Factory for creating a [SettingsViewModel] with a constructor that takes a [UserRepository]
 * and [SettingsPreferences]
 */
class SettingsViewModelFactory(
    private val repository: UserRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, settingsPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}