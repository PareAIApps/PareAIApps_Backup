package pnj.pk.pareaipk.ui.change_profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.databinding.ActivityChangeProfileBinding
import pnj.pk.pareaipk.ui.account.AccountViewModel
import pnj.pk.pareaipk.ui.account.AccountViewModelFactory

class ChangeProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeProfileBinding
    private lateinit var viewModel: AccountViewModel
    private var selectedImageUri: Uri? = null
    private val TAG = "ChangeProfileActivity"

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivProfileImage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Activity created")

        setupViewModel()
        setupUI()
        observeData()
    }

    private fun setupViewModel() {
        try {
            val database = UserRoomDatabase.getDatabase(applicationContext)
            val auth = FirebaseAuth.getInstance()
            val userRepository = UserRepository(database, auth)

            val factory = AccountViewModelFactory(userRepository)
            viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
            Log.d(TAG, "ViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ViewModel: ${e.message}", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        // Set email field as non-editable
        binding.etEmail.isEnabled = false

        // Set click listeners
        binding.btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        // Setup toolbar
//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        binding.toolbar.setNavigationOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
    }

    private fun observeData() {
        viewModel.userProfile.observe(this) { profile ->
            Log.d(TAG, "Profile received: $profile")
            profile?.let {
                binding.etUsername.setText(it.name)
                binding.etEmail.setText(it.email)

                if (!it.profileImageUri.isNullOrEmpty()) {
                    try {
                        Glide.with(this)
                            .load(Uri.parse(it.profileImageUri))
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(binding.ivProfileImage)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image: ${e.message}", e)
                        Glide.with(this)
                            .load(R.drawable.logo)
                            .into(binding.ivProfileImage)
                    }
                } else {
                    Glide.with(this)
                        .load(R.drawable.logo)
                        .into(binding.ivProfileImage)
                }
            }
        }

        viewModel.updateStatus.observe(this) { message ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Update status: $message")

            if (message.contains("berhasil")) {
                finish()
            }
        }
    }

    private fun saveProfile() {
        val name = binding.etUsername.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Saving profile with name: $name, image: $selectedImageUri")
        viewModel.updateProfile(name, selectedImageUri)
    }
}