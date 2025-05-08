package pnj.pk.pareaipk.database.repository

import android.net.Uri
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

        userProfileDao.insertProfile(updatedProfile)
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email
}
