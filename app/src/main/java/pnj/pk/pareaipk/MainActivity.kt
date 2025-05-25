package pnj.pk.pareaipk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var navView: BottomNavigationView
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply user language before setting up UI
        applyUserLanguageAndSetupUI()
    }

    private fun applyUserLanguageAndSetupUI() {
        activityScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                if (currentUser?.email != null) {
                    val database = UserRoomDatabase.getDatabase(this@MainActivity)
                    val userRepository = UserRepository(database, auth)

                    // Ensure user settings are initialized
                    userRepository.initializeUserSettings()

                    // Apply the saved language
                    applyUserLanguage()

                    Log.d(TAG, "Applied user language in MainActivity for: ${currentUser.email}")
                }

                // Setup UI after language is applied
                setupUI()

            } catch (e: Exception) {
                Log.e(TAG, "Error applying language in MainActivity: ${e.message}", e)
                // Setup UI anyway
                setupUI()
            }
        }
    }

    private fun setupUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_scan,
                R.id.navigation_history,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Tangani intent awal
        handleNavigationIntent(intent)
    }

    // ✅ Signature onNewIntent yang benar (tanpa ?)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent) {
        if (intent.getBooleanExtra("navigateToScan", false)) {
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_scan)

            // Update bottom nav agar ikon Scan aktif
            navView.selectedItemId = R.id.navigation_scan
        }
    }
}