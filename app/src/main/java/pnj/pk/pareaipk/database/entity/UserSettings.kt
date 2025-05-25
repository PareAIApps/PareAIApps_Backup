package pnj.pk.pareaipk.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val languageCode: Int = 0, // 0 = English, 1 = Indonesian
    val isNotificationEnabled: Boolean = true
)