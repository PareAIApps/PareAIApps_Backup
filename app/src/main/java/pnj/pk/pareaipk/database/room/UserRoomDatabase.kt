package pnj.pk.pareaipk.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pnj.pk.pareaipk.database.entity.UserProfile  // ✅ Add this line

@Database(entities = [UserProfile::class], version = 1, exportSchema = false)
abstract class UserRoomDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: UserRoomDatabase? = null

        fun getDatabase(context: Context): UserRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    UserRoomDatabase::class.java,
                    "pare_ai_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}