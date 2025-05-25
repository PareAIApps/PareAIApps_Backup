package pnj.pk.pareaipk.database.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.entity.UserSettings
import pnj.pk.pareaipk.database.room.UserRoomDatabase

class UserRepository(
    private val database: UserRoomDatabase,
    private val auth: FirebaseAuth
) {
    private val TAG = "UserRepository"
    private val userProfileDao = database.userProfileDao()
    private val userSettingsDao = database.userSettingsDao()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserProfile(): Flow<UserProfile?> {
        val currentUser = auth.currentUser
        return currentUser?.email?.let { email ->
            userProfileDao.getUserProfile(email)
        } ?: throw IllegalStateException("No logged-in user")
    }

    suspend fun updateUserProfile(
        name: String,
        profileImageUri: Uri? = null
    ) = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No logged-in user")

        val existingProfile = userProfileDao.getUserProfileSync(currentUser.email!!)
        val updatedProfile = UserProfile(
            email = currentUser.email!!,
            name = name,
            profileImageUri = profileImageUri?.toString() ?: existingProfile?.profileImageUri
        )

        userProfileDao.insertProfile(updatedProfile)
        Log.d(TAG, "User profile updated and saved to Room: $updatedProfile")
    }

    suspend fun saveUserProfile(userProfile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertProfile(userProfile)
        Log.d(TAG, "User profile saved to Room: $userProfile")
    }

    suspend fun getUserProfileSync(email: String): UserProfile? = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfileSync(email)
        Log.d(TAG, "Retrieved user profile for $email: $profile")
        return@withContext profile
    }

    // User Settings methods
    fun getCurrentUserSettings(): Flow<UserSettings?> {
        val currentUser = auth.currentUser
        return currentUser?.email?.let { email ->
            userSettingsDao.getUserSettings(email)
        } ?: throw IllegalStateException("No logged-in user")
    }

    suspend fun getUserSettingsSync(email: String): UserSettings? = withContext(Dispatchers.IO) {
        return@withContext userSettingsDao.getUserSettingsSync(email)
    }

    suspend fun initializeUserSettings() = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser ?: return@withContext
        val email = currentUser.email ?: return@withContext

        val existingSettings = userSettingsDao.getUserSettingsSync(email)
        if (existingSettings == null) {
            val defaultSettings = UserSettings(
                email = email,
                languageCode = 0, // Default to English
                isNotificationEnabled = true
            )
            userSettingsDao.insertSettings(defaultSettings)
            Log.d(TAG, "Initialized default settings for $email")
        }
    }

    suspend fun updateLanguageSetting(languageCode: Int) = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser ?: return@withContext
        val email = currentUser.email ?: return@withContext

        userSettingsDao.updateLanguage(email, languageCode)
        Log.d(TAG, "Updated language to $languageCode for $email")
    }

    suspend fun updateNotificationSetting(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser ?: return@withContext
        val email = currentUser.email ?: return@withContext

        userSettingsDao.updateNotification(email, isEnabled)
        Log.d(TAG, "Updated notification to $isEnabled for $email")
    }

    fun logout() {
        auth.signOut()
        Log.d(TAG, "User logged out from Firebase Auth")
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email
}