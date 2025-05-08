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

class DetailHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository
    private val _scanHistoryItem = MutableLiveData<HistoryEntity>()
    val scanHistoryItem: LiveData<HistoryEntity> = _scanHistoryItem

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"
        val scanHistoryDao = HistoryDatabase.getDatabase(application, userId).scanHistoryDao()
        repository = HistoryRepository(scanHistoryDao)
    }

    fun loadScanHistoryById(id: Long) = viewModelScope.launch {
        _scanHistoryItem.value = repository.getScanHistoryById(id)
    }
}