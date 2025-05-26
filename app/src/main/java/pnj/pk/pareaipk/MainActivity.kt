package pnj.pk.pareaipk

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import pnj.pk.pareaipk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_article,
                R.id.navigation_history,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Override bottom navigation behavior untuk handling scan
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        navController.navigate(R.id.navigation_home)
                    }
                    true
                }
                R.id.navigation_article -> {
                    if (navController.currentDestination?.id != R.id.navigation_article) {
                        navController.navigate(R.id.navigation_article)
                    }
                    true
                }
                R.id.navigation_history -> {
                    if (navController.currentDestination?.id != R.id.navigation_history) {
                        navController.navigate(R.id.navigation_history)
                    }
                    true
                }
                R.id.navigation_settings -> {
                    if (navController.currentDestination?.id != R.id.navigation_settings) {
                        navController.navigate(R.id.navigation_settings)
                    }
                    true
                }
                else -> false
            }
        }

        // Floating scan button click - only navigate if not already on scan fragment
        binding.floatingScanButton.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_scan) {
                navController.navigate(R.id.navigation_scan)
            }
            // If already on scan fragment, do nothing (no reaction)
        }

        // Handle destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> navView.selectedItemId = R.id.navigation_home
                R.id.navigation_article -> navView.selectedItemId = R.id.navigation_article
                R.id.navigation_history -> navView.selectedItemId = R.id.navigation_history
                R.id.navigation_settings -> navView.selectedItemId = R.id.navigation_settings
                R.id.navigation_scan -> {
                    // Clear selection untuk scan fragment
                    navView.menu.setGroupCheckable(0, true, false)
                    for (i in 0 until navView.menu.size()) {
                        navView.menu.getItem(i).isChecked = false
                    }
                    navView.menu.setGroupCheckable(0, true, true)
                }
            }
        }

        handleNavigationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent) {
        if (intent.getBooleanExtra("navigateToScan", false)) {
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_scan)
        }
    }
}