package pnj.pk.pareaipk.database.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.room.UserRoomDatabase

class UserRepository(
    private val database: UserRoomDatabase,
    private val auth: FirebaseAuth
) {
    private val TAG = "UserRepository"
    private val userProfileDao = database.userProfileDao()

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

        // Ensure the data is saved to Room database
        userProfileDao.insertProfile(updatedProfile)
        Log.d(TAG, "User profile updated and saved to Room: $updatedProfile")
    }

    // Add this method to save a user profile directly
    suspend fun saveUserProfile(userProfile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertProfile(userProfile)
        Log.d(TAG, "User profile saved to Room: $userProfile")
    }

    // Add this method to check if a profile exists synchronously
    suspend fun getUserProfileSync(email: String): UserProfile? = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getUserProfileSync(email)
        Log.d(TAG, "Retrieved user profile for $email: $profile")
        return@withContext profile
    }

    fun logout() {
        // The fix is to NOT reset the database when logging out
        // Just sign out from Firebase Auth
        auth.signOut()
        Log.d(TAG, "User logged out from Firebase Auth")
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email
}