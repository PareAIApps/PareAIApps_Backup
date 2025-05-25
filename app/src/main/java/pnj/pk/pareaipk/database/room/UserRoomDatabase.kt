package pnj.pk.pareaipk.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pnj.pk.pareaipk.database.entity.UserProfile
import pnj.pk.pareaipk.database.entity.UserSettings

@Database(entities = [UserProfile::class, UserSettings::class], version = 2, exportSchema = false)
abstract class UserRoomDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: UserRoomDatabase? = null

        // Migration dari versi 1 ke 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_settings` (
                        `email` TEXT NOT NULL,
                        `languageCode` INTEGER NOT NULL DEFAULT 0,
                        `isNotificationEnabled` INTEGER NOT NULL DEFAULT 1,
                        PRIMARY KEY(`email`)
                    )
                    """
                )
            }
        }

        fun getDatabase(context: Context): UserRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    UserRoomDatabase::class.java,
                    "pare_ai_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}