package pnj.pk.pareaipk.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pnj.pk.pareaipk.databinding.FragmentSettingsBinding
import pnj.pk.pareaipk.ui.account.AccountViewModel
import pnj.pk.pareaipk.ui.account.AccountViewModelFactory
import pnj.pk.pareaipk.ui.login.LoginActivity
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.ui.about.AboutActivity
import pnj.pk.pareaipk.ui.account.AccountActivity

class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var viewModel: AccountViewModel
    private lateinit var userRepository: UserRepository

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize repository
        val database = context?.let { UserRoomDatabase.getDatabase(it) }
        val auth = FirebaseAuth.getInstance()
        database?.let {
            userRepository = UserRepository(it, auth)
            setupViewModel()
        }

        // Set click listeners for Account and About menu options
        binding.btnAccount.setOnClickListener {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbout.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


    private fun setupViewModel() {
        try {
            val factory = AccountViewModelFactory(userRepository)
            viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]

            // Set click listener for logout button
            binding.btnLogout.setOnClickListener {
                logout()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ViewModel: ${e.message}", e)
        }
    }

    private fun logout() {
        try {
            Log.d(TAG, "Logging out user")
            // Important: We're just calling logout() which now just signs out from Firebase
            // User data remains in Room database
            viewModel.logout()

            // Navigate to login screen
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}