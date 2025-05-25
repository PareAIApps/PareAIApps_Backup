package pnj.pk.pareaipk.ui.settings

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.FragmentSettingsBinding
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.ui.about.AboutActivity
import pnj.pk.pareaipk.ui.account.AccountActivity
import pnj.pk.pareaipk.ui.account.AccountViewModel
import pnj.pk.pareaipk.ui.account.AccountViewModelFactory
import pnj.pk.pareaipk.ui.login.LoginActivity
import pnj.pk.pareaipk.ui.share_apps.ShareAppsActivity
import java.util.*

class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var viewModel: AccountViewModel
    private lateinit var userRepository: UserRepository

    private val binding get() = _binding!!

    // Request notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkAndUpdateNotificationState(true)
        } else {
            binding.switchNotification.isChecked = false
            lifecycleScope.launch {
                userRepository.updateNotificationSetting(false)
            }
        }
    }

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

            // Initialize user settings
            initializeUserSettings()
        }

        setupClickListeners()
        setupNotificationSwitch()

        return binding.root
    }

    private fun initializeUserSettings() {
        lifecycleScope.launch {
            try {
                // Initialize user settings if not exists
                userRepository.initializeUserSettings()
                Log.d(TAG, "User settings initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing user settings: ${e.message}", e)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAccount.setOnClickListener {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)
        }

        binding.btnShare.setOnClickListener {
            val intent = Intent(requireContext(), ShareAppsActivity::class.java)
            startActivity(intent)
        }

        binding.btnAbout.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        binding.btnTranslate.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_language_selection, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group_language)
        val radioIndonesia = dialogView.findViewById<RadioButton>(R.id.radio_indonesia)
        val radioEnglish = dialogView.findViewById<RadioButton>(R.id.radio_english)

        lifecycleScope.launch {
            try {
                val userSettings = userRepository.getCurrentUserSettings().first()
                val currentLanguageCode = userSettings?.languageCode ?: 0

                Log.d(TAG, "Current language code: $currentLanguageCode")

                // Reset radio buttons
                radioIndonesia.isChecked = false
                radioEnglish.isChecked = false

                // Set radio button based on saved language code
                when (currentLanguageCode) {
                    1 -> {
                        radioIndonesia.isChecked = true
                        Log.d(TAG, "Set Indonesian radio button as checked")
                    }
                    0 -> {
                        radioEnglish.isChecked = true
                        Log.d(TAG, "Set English radio button as checked")
                    }
                }

                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.select_language))
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.apply)) { _, _ ->
                        val selectedId = radioGroup.checkedRadioButtonId
                        when (selectedId) {
                            R.id.radio_indonesia -> changeLanguage(1) // Indonesian
                            R.id.radio_english -> changeLanguage(0)   // English
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.green_light)
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error in showLanguageDialog: ${e.message}", e)
            }
        }
    }

    private fun changeLanguage(languageCode: Int) {
        Log.d(TAG, "Changing language to code: $languageCode")

        lifecycleScope.launch {
            try {
                // Save language preference to Room database
                userRepository.updateLanguageSetting(languageCode)
                Log.d(TAG, "Language code saved successfully: $languageCode")

                // Apply the language change immediately
                val languageString = if (languageCode == 1) "id" else "en"
                updateAppLocale(languageString)

                // Restart activity to fully apply language change to all UI elements
                activity?.recreate()

            } catch (e: Exception) {
                Log.e(TAG, "Error saving language: ${e.message}", e)
            }
        }
    }

    private fun updateAppLocale(languageCode: String) {
        try {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(requireContext().resources.configuration)
            config.setLocale(locale)

            requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

            // Update application context as well
            if (requireContext() != requireContext().applicationContext) {
                requireContext().applicationContext.resources.updateConfiguration(
                    config,
                    requireContext().applicationContext.resources.displayMetrics
                )
            }

            Log.d(TAG, "App locale updated to: $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app locale: ${e.message}", e)
        }
    }

    private fun setupNotificationSwitch() {
        // Load current notification setting from Room database
        lifecycleScope.launch {
            try {
                val userSettings = userRepository.getCurrentUserSettings().first()
                val isEnabled = userSettings?.isNotificationEnabled ?: true
                binding.switchNotification.isChecked = isEnabled
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notification setting: ${e.message}", e)
                binding.switchNotification.isChecked = true // Default
            }
        }

        // Set change listener for notification switch
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                        checkAndUpdateNotificationState(true)
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                checkAndUpdateNotificationState(isChecked)
            }
        }
    }

    private fun checkAndUpdateNotificationState(isEnabled: Boolean) {
        lifecycleScope.launch {
            try {
                userRepository.updateNotificationSetting(isEnabled)
                if (isEnabled) {
                    DailyReminderWorker.scheduleReminder(requireContext())
                } else {
                    DailyReminderWorker.cancelReminder(requireContext())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification state: ${e.message}", e)
            }
        }
    }

    private fun setupViewModel() {
        try {
            val factory = AccountViewModelFactory(userRepository)
            viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]

            binding.btnLogout.setOnClickListener {
                logout()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ViewModel: ${e.message}", e)
        }
    }

    private fun logout() {
        try {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout_dialog_title))
                .setMessage(getString(R.string.logout_dialog_message))
                .setPositiveButton(getString(R.string.logout_dialog_positive)) { _, _ ->
                    Log.d(TAG, "Logging out user")
                    viewModel.logout()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                }
                .setNegativeButton(getString(R.string.logout_dialog_negative)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .create()

            dialog.show()

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}