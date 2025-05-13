package pnj.pk.pareaipk.ui.change_profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.databinding.ActivityChangeProfileBinding
import pnj.pk.pareaipk.utils.ProfileImageUtils

class ChangeProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeProfileBinding
    private lateinit var viewModel: ChangeProfileViewModel
    private var selectedImageUri: Uri? = null
    private val TAG = "ChangeProfileActivity"

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                processSelectedImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure back navigation
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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

            val factory = ChangeProfileViewModelFactory(userRepository)
            viewModel = ViewModelProvider(this, factory)[ChangeProfileViewModel::class.java]
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
            showConfirmationDialog()
        }
    }

    private fun processSelectedImage(uri: Uri) {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE

        // Process image in background
        Thread {
            try {
                // First optimize the image
                val optimizedUri = ProfileImageUtils.optimizeProfileImage(applicationContext, uri)

                // Update UI on main thread
                runOnUiThread {
                    selectedImageUri = optimizedUri ?: uri

                    // Setup Glide with proper error handling
                    val requestOptions = RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache during edit
                        .skipMemoryCache(true) // Important for fresh edits
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)

                    try {
                        Glide.with(this)
                            .load(selectedImageUri)
                            .apply(requestOptions)
                            .into(binding.ivProfileImage)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image with Glide: ${e.message}", e)
                        binding.ivProfileImage.setImageResource(R.drawable.logo)
                    }

                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing selected image: ${e.message}", e)

                // Update UI on main thread
                runOnUiThread {
                    selectedImageUri = uri // Use original if optimization fails
                    binding.ivProfileImage.setImageResource(R.drawable.logo)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ChangeProfileActivity,
                        "Error processing image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun observeData() {
        // Observe user profile
        viewModel.userProfile.observe(this) { profile ->
            Log.d(TAG, "Profile received: $profile")
            profile?.let {
                binding.etUsername.setText(it.name)
                binding.etEmail.setText(it.email)

                if (!it.profileImageUri.isNullOrEmpty()) {
                    loadProfileImage(it.profileImageUri)
                } else {
                    binding.ivProfileImage.setImageResource(R.drawable.logo)
                }
            }
        }

        // Observe update status
        viewModel.updateStatus.observe(this) { message ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Update status: $message")

            if (message.contains("berhasil")) {
                finish()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadProfileImage(imageUriString: String) {
        try {
            // Process in background
            Thread {
                val uri = Uri.parse(imageUriString)
                val optimizedUri = ProfileImageUtils.optimizeProfileImage(applicationContext, uri)

                // Update UI on main thread
                runOnUiThread {
                    try {
                        val glideTarget = optimizedUri ?: uri

                        val requestOptions = RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)

                        Glide.with(this)
                            .load(glideTarget)
                            .apply(requestOptions)
                            .into(binding.ivProfileImage)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading profile image: ${e.message}", e)
                        binding.ivProfileImage.setImageResource(R.drawable.logo)
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing profile image URI: ${e.message}", e)
            binding.ivProfileImage.setImageResource(R.drawable.logo)
        }
    }

    private fun showConfirmationDialog() {
        val name = binding.etUsername.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Konfirmasi Perubahan")
            .setMessage("Apakah Anda yakin ingin mengubah data profil?")
            .setPositiveButton("Ya") { _, _ ->
                // If user selects Yes, proceed with saving profile
                saveProfile()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                // If user selects No, close dialog without changes
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()

        // Change "Yes" and "No" button colors to green_light
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        val greenColor = resources.getColor(R.color.green_light, theme)
        positiveButton.setTextColor(greenColor)
        negativeButton.setTextColor(greenColor)
    }

    private fun saveProfile() {
        val name = binding.etUsername.text.toString().trim()

        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Saving profile with name: $name, image: $selectedImageUri")
        viewModel.updateProfile(name, selectedImageUri)
    }
}