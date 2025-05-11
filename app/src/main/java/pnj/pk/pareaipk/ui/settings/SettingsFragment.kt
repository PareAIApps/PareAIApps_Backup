package pnj.pk.pareaipk.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.databinding.FragmentSettingsBinding
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.ui.about.AboutActivity
import pnj.pk.pareaipk.ui.account.AccountActivity
import pnj.pk.pareaipk.ui.account.AccountViewModel
import pnj.pk.pareaipk.ui.account.AccountViewModelFactory
import pnj.pk.pareaipk.ui.login.LoginActivity

class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var viewModel: AccountViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var settingsPreferences: SettingsPreferences

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Request notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission is granted, schedule notifications if enabled
            checkAndUpdateNotificationState(true)
        } else {
            // Permission denied, turn off the switch
            binding.switchNotification.isChecked = false
            lifecycleScope.launch {
                settingsPreferences.setNotificationEnabled(false)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize Settings Preferences
        settingsPreferences = SettingsPreferences.getInstance(requireContext())

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

        // Set up notification switch
        setupNotificationSwitch()

        return binding.root
    }

    private fun setupNotificationSwitch() {
        // Load current notification setting
        lifecycleScope.launch {
            val isEnabled = settingsPreferences.isNotificationEnabled.first()
            binding.switchNotification.isChecked = isEnabled
        }

        // Set change listener for notification switch
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Check and request notification permission for Android 13+
                when {
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                        // Permission already granted
                        checkAndUpdateNotificationState(true)
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                        // Show rationale and request permission
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> {
                        // First time asking or user selected "Don't ask again"
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // For Android below 13 or turning off notifications
                checkAndUpdateNotificationState(isChecked)
            }
        }
    }

    private fun checkAndUpdateNotificationState(isEnabled: Boolean) {
        lifecycleScope.launch {
            settingsPreferences.setNotificationEnabled(isEnabled)
            if (isEnabled) {
                DailyReminderWorker.scheduleReminder(requireContext())
            } else {
                DailyReminderWorker.cancelReminder(requireContext())
            }
        }
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