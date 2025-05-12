package pnj.pk.pareaipk.ui.change_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pnj.pk.pareaipk.database.repository.UserRepository

class ChangeProfileViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangeProfileViewModel::class.java)) {
            return ChangeProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}