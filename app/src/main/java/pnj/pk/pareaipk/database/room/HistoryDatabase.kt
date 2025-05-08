package pnj.pk.pareaipk.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pnj.pk.pareaipk.database.entity.HistoryEntity

@Database(entities = [HistoryEntity::class], version = 4, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCES: MutableMap<String, HistoryDatabase> = mutableMapOf()

        fun getDatabase(context: Context, userId: String): HistoryDatabase {
            return INSTANCES[userId] ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryDatabase::class.java,
                    "scan_history_database_$userId"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCES[userId] = instance
                instance
            }
        }
    }
}