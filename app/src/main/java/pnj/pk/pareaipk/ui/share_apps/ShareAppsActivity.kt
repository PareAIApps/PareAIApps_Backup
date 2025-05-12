package pnj.pk.pareaipk.ui.share_apps

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import java.io.File

class ShareAppsActivity : AppCompatActivity() {

    private lateinit var viewModel: ShareAppsViewModel
    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var userImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_apps)
        supportActionBar?.hide()

        // Initialize views
        userNameTextView = findViewById(R.id.user_name)
        userPhoneTextView = findViewById(R.id.user_phone)
        userImageView = findViewById(R.id.user_image)

        // Initialize repository
        val database = UserRoomDatabase.getDatabase(applicationContext)
        val auth = FirebaseAuth.getInstance()
        val userRepository = UserRepository(database, auth)

        // Initialize ViewModel
        val factory = ShareAppsViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[ShareAppsViewModel::class.java]

        // Setup toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Observe user profile
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let { userProfile ->
                // Show name
                userNameTextView.text = userProfile.name

                // Show email
                val currentUser = FirebaseAuth.getInstance().currentUser
                userPhoneTextView.text = currentUser?.email ?: "No email"

                // Load image using Glide with robust error handling
                val imageUri = userProfile.profileImageUri
                loadProfileImage(imageUri)
            }
        }
    }

    private fun loadProfileImage(imageUriString: String?) {
        try {
            if (!imageUriString.isNullOrBlank()) {
                val uri = Uri.parse(imageUriString)

                // Try different loading strategies
                val loadImage: () -> Unit = {
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .into(userImageView)
                }

                // Strategy 1: Check if it's a file URI and file exists
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: "")
                    if (file.exists()) {
                        loadImage()
                        return
                    }
                }

                // Strategy 2: Try to get a file from content URI
                try {
                    val file = getFileFromContentUri(this, uri)
                    if (file != null && file.exists()) {
                        Glide.with(this)
                            .load(file)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(userImageView)
                        return
                    }
                } catch (e: Exception) {
                    Log.e("ShareAppsActivity", "Error converting content URI to file: ${e.message}")
                }

                // Strategy 3: Try direct content URI loading
                loadImage()
            } else {
                // No image URI, set default
                userImageView.setImageResource(R.drawable.logo)
            }
        } catch (e: Exception) {
            Log.e("ShareAppsActivity", "Error loading profile image: ${e.message}")
            userImageView.setImageResource(R.drawable.logo)
        }
    }

    // Utility method to convert content URI to File
    private fun getFileFromContentUri(context: Context, contentUri: Uri): File? {
        return try {
            // Create a temporary file
            val tempFile = File.createTempFile("profile_image", ".jpg", context.cacheDir)
            tempFile.deleteOnExit()

            // Open input stream from content URI
            val inputStream = context.contentResolver.openInputStream(contentUri)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e("ShareAppsActivity", "Error converting content URI to file: ${e.message}")
            null
        }
    }

    // Refresh profile when returning to this activity
    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
    }
}