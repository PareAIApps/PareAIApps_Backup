package pnj.pk.pareaipk.ui.privacy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up back button functionality
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    // Handle back press (for API compatibility)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}