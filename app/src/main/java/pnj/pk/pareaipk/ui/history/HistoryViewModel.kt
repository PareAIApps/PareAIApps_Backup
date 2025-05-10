package pnj.pk.pareaipk.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.entity.HistoryEntity
import pnj.pk.pareaipk.database.repository.HistoryRepository
import pnj.pk.pareaipk.database.room.HistoryDatabase

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository
    val allScanHistory: LiveData<List<HistoryEntity>>

    // Store the full list of history items for filtering
    private val _fullHistoryList = MutableLiveData<List<HistoryEntity>>()
    val fullHistoryList: LiveData<List<HistoryEntity>> = _fullHistoryList

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        val scanHistoryDao = HistoryDatabase.getDatabase(application, userId).scanHistoryDao()
        repository = HistoryRepository(scanHistoryDao)
        allScanHistory = repository.allScanHistory
    }

    fun setFullHistoryList(list: List<HistoryEntity>) {
        _fullHistoryList.value = list
    }

    fun insertScanHistory(scanHistory: HistoryEntity) = viewModelScope.launch {
        repository.insertScanHistory(scanHistory)
    }

    fun deleteScanHistory(scanHistory: HistoryEntity) = viewModelScope.launch {
        repository.deleteScanHistory(scanHistory)
    }
}