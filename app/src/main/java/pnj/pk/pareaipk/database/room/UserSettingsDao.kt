package pnj.pk.pareaipk.database.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pnj.pk.pareaipk.database.entity.UserSettings

@Dao
interface UserSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)

    @Update
    suspend fun updateSettings(settings: UserSettings)

    @Query("SELECT * FROM user_settings WHERE email = :email LIMIT 1")
    fun getUserSettings(email: String): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE email = :email LIMIT 1")
    suspend fun getUserSettingsSync(email: String): UserSettings?

    @Query("UPDATE user_settings SET languageCode = :languageCode WHERE email = :email")
    suspend fun updateLanguage(email: String, languageCode: Int)

    @Query("UPDATE user_settings SET isNotificationEnabled = :isEnabled WHERE email = :email")
    suspend fun updateNotification(email: String, isEnabled: Boolean)
}