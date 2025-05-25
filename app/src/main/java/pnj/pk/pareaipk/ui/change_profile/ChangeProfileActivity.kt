package pnj.pk.pareaipk.ui.change_profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChangeProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeProfileBinding
    private lateinit var viewModel: ChangeProfileViewModel
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private val TAG = "ChangeProfileActivity"

    // Request permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, getString(R.string.toast_permission_camera_required), Toast.LENGTH_SHORT).show()
        }
    }

    // Pick image from gallery launcher
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

    // Take photo with camera launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val photoFile = File(path)
                val photoUri = Uri.fromFile(photoFile)
                processSelectedImage(photoUri)
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
            Toast.makeText(this, getString(R.string.toast_error_initializing_viewmodel, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        // Set email field as non-editable
        binding.etEmail.isEnabled = false

        // Set click listeners
        binding.btnChangeImage.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnSaveProfile.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(getString(R.string.dialog_option_camera), getString(R.string.dialog_option_gallery))

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_image_source_title))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                }
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()

        // Ubah warna tombol "Batal" setelah dialog muncul
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.green_light)
        )
    }


    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Create the File where the photo should go
        try {
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(takePictureIntent)
        } catch (ex: IOException) {
            Log.e(TAG, "Error creating image file: ${ex.message}", ex)
            Toast.makeText(this, getString(R.string.toast_error_create_image_file), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir("Pictures")
        return File.createTempFile(
            "PROFILE_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
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
                        getString(R.string.toast_error_processing_image, e.message),
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
            Toast.makeText(this, getString(R.string.toast_name_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirmation_title))
            .setMessage(getString(R.string.dialog_confirmation_message))
            .setPositiveButton(getString(R.string.dialog_positive_button_yes)) { _, _ ->
                // If user selects Yes, proceed with saving profile
                saveProfile()
            }
            .setNegativeButton(getString(R.string.dialog_negative_button_no)) { dialog, _ ->
                // If user selects No, close dialog without changes
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()

        // Change "Yes" and "No" button colors to green_light
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        val redColor = resources.getColor(R.color.red, theme)
        val greenColor = resources.getColor(R.color.green_light, theme)
        positiveButton.setTextColor(redColor)
        negativeButton.setTextColor(greenColor)
    }

    private fun saveProfile() {
        val name = binding.etUsername.text.toString().trim()

        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "Saving profile with name: $name, image: $selectedImageUri")
        viewModel.updateProfile(name, selectedImageUri)
    }
}