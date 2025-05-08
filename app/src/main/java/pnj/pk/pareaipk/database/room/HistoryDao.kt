package pnj.pk.pareaipk.database.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pnj.pk.pareaipk.database.entity.HistoryEntity


@Dao
interface HistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY id DESC")
    fun getAllScanHistory(): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanHistoryById(id: Long): HistoryEntity?

    @Insert
    suspend fun insertScanHistory(scanHistory: HistoryEntity)

    @Delete
    suspend fun deleteScanHistory(scanHistory: HistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAllScanHistory()
}