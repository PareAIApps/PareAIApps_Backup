package pnj.pk.pareaipk.ui.share_apps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.repository.UserRepository
import pnj.pk.pareaipk.database.room.UserRoomDatabase
import pnj.pk.pareaipk.utils.ProfileImageUtils

class ShareAppsActivity : AppCompatActivity() {

    private lateinit var viewModel: ShareAppsViewModel
    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var userImageView: ImageView
    private lateinit var shareButton: Button
    private val TAG = "ShareAppsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_apps)
        supportActionBar?.hide()

        // Initialize views
        userNameTextView = findViewById(R.id.user_name)
        userPhoneTextView = findViewById(R.id.user_phone)
        userImageView = findViewById(R.id.user_image)
        shareButton = findViewById(R.id.share_button)

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

        // Setup share button
        setupShareButton()

        // Observe user profile
        observeUserProfile()
    }

    private fun setupShareButton() {
        shareButton.setOnClickListener {
            val appLink = "https://drive.google.com/file/d/1cn2II3JWlWXCMh60UbLT_h0tBzNBv7En/view?usp=sharing"

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, appLink)
                type = "text/plain"
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let { userProfile ->
                // Show name
                userNameTextView.text = userProfile.name

                // Show email
                val currentUser = FirebaseAuth.getInstance().currentUser
                userPhoneTextView.text = currentUser?.email ?: "No email"

                // Load image using optimized method
                val imageUri = userProfile.profileImageUri
                loadProfileImageSafely(imageUri)
            }
        }
    }

    private fun loadProfileImageSafely(imageUriString: String?) {
        try {
            Log.d(TAG, "Attempting to load profile image: $imageUriString")

            // Configure Glide request options
            val requestOptions = RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)

            if (!imageUriString.isNullOrBlank()) {
                // First attempt: Try with our custom ProfileImageUtils
                try {
                    val originalUri = Uri.parse(imageUriString)

                    // Process the image in a background thread to avoid ANRs
                    Thread {
                        val optimizedUri = ProfileImageUtils.optimizeProfileImage(applicationContext, originalUri)

                        // Update the UI on the main thread
                        runOnUiThread {
                            if (optimizedUri != null) {
                                Glide.with(applicationContext)
                                    .load(optimizedUri)
                                    .apply(requestOptions)
                                    .into(userImageView)
                                Log.d(TAG, "Successfully loaded optimized image")
                            } else {
                                loadWithDefaultGlide(imageUriString, requestOptions)
                            }
                        }
                    }.start()
                } catch (e: Exception) {
                    Log.e(TAG, "Error with custom image loading: ${e.message}", e)
                    // Fall back to standard Glide loading
                    loadWithDefaultGlide(imageUriString, requestOptions)
                }
            } else {
                // No image URI, set default
                Log.d(TAG, "No image URI provided, using default")
                Glide.with(applicationContext)
                    .load(R.drawable.logo)
                    .apply(requestOptions)
                    .into(userImageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}", e)

            // Fallback to default image when there's an error
            try {
                Glide.with(applicationContext)
                    .load(R.drawable.logo)
                    .into(userImageView)
            } catch (e: Exception) {
                Log.e(TAG, "Even fallback image loading failed: ${e.message}", e)
                // Last resort - set directly
                userImageView.setImageResource(R.drawable.logo)
            }
        }
    }

    private fun loadWithDefaultGlide(imageUriString: String, requestOptions: RequestOptions) {
        try {
            Log.d(TAG, "Falling back to standard Glide loading")
            val uri = Uri.parse(imageUriString)

            // Apply additional transformations for small images
            val updatedOptions = requestOptions.override(120, 120)

            Glide.with(applicationContext)
                .load(uri)
                .apply(updatedOptions)
                .into(userImageView)
        } catch (e: Exception) {
            Log.e(TAG, "Default Glide loading failed: ${e.message}", e)
            setDefaultImage()
        }
    }

    private fun setDefaultImage() {
        try {
            Glide.with(applicationContext)
                .load(R.drawable.logo)
                .into(userImageView)
        } catch (e: Exception) {
            // Direct fallback
            userImageView.setImageResource(R.drawable.logo)
        }
    }

    // Refresh profile when returning to this activity
    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
    }
}