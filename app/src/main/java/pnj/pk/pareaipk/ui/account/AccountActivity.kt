package pnj.pk.pareaipk.ui.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.databinding.ActivityAccountBinding
import pnj.pk.pareaipk.ui.change_profile.ChangeProfileActivity

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AccountViewModel
    private val TAG = "AccountActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Mengikat layout ke Activity
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Menyesuaikan padding dengan system bar (untuk tampilan full screen)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi repository untuk mendapatkan data pengguna
        val database = UserRoomDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database, FirebaseAuth.getInstance())

        // Setup ViewModel
        setupViewModel()

        // Observe user profile data
        observeUserProfile()

        // Mengatur aksi klik untuk mengedit profil
        binding.layoutEditProfile.setOnClickListener {
            // Menavigasi ke activity untuk mengubah profil
            startActivity(Intent(this, ChangeProfileActivity::class.java))
        }
    }

    private fun setupViewModel() {
        try {
            val factory = AccountViewModelFactory(userRepository)
            viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
            Log.d(TAG, "ViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ViewModel: ${e.message}", e)
        }
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(this) { profile ->
            Log.d(TAG, "Profile received in AccountActivity: $profile")
            profile?.let {
                binding.tvAccountName.text = it.name
                binding.tvEmail.text = it.email

                // Load profile image
                if (!it.profileImageUri.isNullOrEmpty()) {
                    try {
                        Glide.with(this)
                            .load(Uri.parse(it.profileImageUri))
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(binding.imgProfile)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image: ${e.message}", e)
                        loadDefaultImage()
                    }
                } else {
                    loadDefaultImage()
                }
            }
        }
    }

    private fun loadDefaultImage() {
        Glide.with(this)
            .load(R.drawable.logo)
            .into(binding.imgProfile)
    }

    // Refresh data when returning to this activity
    override fun onResume() {
        super.onResume()
        // This will refresh the profile data when returning from ChangeProfileActivity
        viewModel.refreshProfile()
    }
}