package pnj.pk.pareaipk.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pnj.pk.pareaipk.database.repository.UserRepository

class AccountViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}