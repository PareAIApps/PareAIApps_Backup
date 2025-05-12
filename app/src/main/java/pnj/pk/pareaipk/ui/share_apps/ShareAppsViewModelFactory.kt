package pnj.pk.pareaipk.ui.share_apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pnj.pk.pareaipk.database.repository.UserRepository

class ShareAppsViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareAppsViewModel::class.java)) {
            return ShareAppsViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}