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
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.databinding.ActivityChangeProfileBinding

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

        // Mengatur fungsi navigasi back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Kembali ke halaman sebelumnya
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

    private fun observeData() {
        // Observe user profile
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
                // Jika user memilih Ya, lanjutkan dengan menyimpan profil
                saveProfile()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                // Jika user memilih Tidak, tutup dialog tanpa melakukan perubahan
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()

        // Mengubah warna tombol Ya dan Tidak menjadi green_light
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