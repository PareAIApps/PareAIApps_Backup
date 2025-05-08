package pnj.pk.pareaipk.database.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pnj.pk.pareaipk.database.entity.HistoryEntity
import pnj.pk.pareaipk.database.room.HistoryDao

class HistoryRepository(private val scanHistoryDao: HistoryDao) {
    val allScanHistory: LiveData<List<HistoryEntity>> = scanHistoryDao.getAllScanHistory()

    suspend fun insertScanHistory(scanHistory: HistoryEntity) {
        withContext(Dispatchers.IO) {
            scanHistoryDao.insertScanHistory(scanHistory)
        }
    }

    suspend fun getScanHistoryById(id: Long): HistoryEntity? {
        return withContext(Dispatchers.IO) {
            scanHistoryDao.getScanHistoryById(id)
        }
    }

    suspend fun deleteScanHistory(scanHistory: HistoryEntity) {
        withContext(Dispatchers.IO) {
            scanHistoryDao.deleteScanHistory(scanHistory)
        }
    }

    suspend fun deleteAllScanHistory() {
        withContext(Dispatchers.IO) {
            scanHistoryDao.deleteAllScanHistory()
        }
    }
}