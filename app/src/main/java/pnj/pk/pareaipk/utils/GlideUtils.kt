package pnj.pk.pareaipk.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import pnj.pk.pareaipk.R

/**
 * Utility class for standardizing Glide image loading operations
 * throughout the application, with specific focus on profile images.
 */
object GlideUtils {
    private const val TAG = "GlideUtils"

    /**
     * Load profile image safely with proper error handling and optimization
     */
    fun loadProfileImage(
        context: Context,
        imageView: ImageView,
        imageUriString: String?,
        skipCache: Boolean = false
    ) {
        try {
            if (imageUriString.isNullOrBlank()) {
                setDefaultImage(context, imageView)
                return
            }

            // Parse URI first
            val originalUri = try {
                Uri.parse(imageUriString)
            } catch (e: Exception) {
                Log.e(TAG, "Invalid URI format: $imageUriString", e)
                setDefaultImage(context, imageView)
                return
            }

            // Process in background thread
            Thread {
                try {
                    // Optimize image first
                    val optimizedUri = ProfileImageUtils.optimizeProfileImage(context, originalUri)
                    val finalUri = optimizedUri ?: originalUri

                    // Build request options
                    val requestOptions = RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)

                    // Configure caching based on need
                    if (skipCache) {
                        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                    } else {
                        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                    }

                    // Load on main thread
                    try {
                        // Need to check if context is still valid
                        if (isContextValid(context)) {
                            Glide.with(context)
                                .load(finalUri)
                                .apply(requestOptions)
                                .into(imageView)

                            Log.d(TAG, "Successfully loaded image from $finalUri")
                        } else {
                            Log.w(TAG, "Context is no longer valid")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in Glide image loading: ${e.message}", e)
                        // Try direct setting on main thread if possible
                        try {
                            if (isContextValid(context)) {
                                Glide.with(context)
                                    .load(R.drawable.logo)
                                    .into(imageView)
                            }
                        } catch (e2: Exception) {
                            Log.e(TAG, "Failed to set fallback image", e2)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing image: ${e.message}", e)
                    setDefaultImageOnMainThread(context, imageView)
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in loadProfileImage: ${e.message}", e)
            setDefaultImage(context, imageView)
        }
    }

    /**
     * Set default image immediately on the current thread
     */
    private fun setDefaultImage(context: Context, imageView: ImageView) {
        try {
            if (isContextValid(context)) {
                Glide.with(context)
                    .load(R.drawable.logo)
                    .into(imageView)
            } else {
                // Last resort - direct resource set
                imageView.setImageResource(R.drawable.logo)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting default image: ${e.message}", e)
            try {
                imageView.setImageResource(R.drawable.logo)
            } catch (e2: Exception) {
                Log.e(TAG, "Complete failure setting default image", e2)
            }
        }
    }

    /**
     * Set default image on the main thread
     */
    private fun setDefaultImageOnMainThread(context: Context, imageView: ImageView) {
        try {
            if (context is android.app.Activity) {
                context.runOnUiThread {
                    setDefaultImage(context, imageView)
                }
            } else {
                // Try our best for non-Activity contexts
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    setDefaultImage(context, imageView)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting to main thread: ${e.message}", e)
        }
    }

    /**
     * Check if a context is still valid and not destroyed
     */
    private fun isContextValid(context: Context): Boolean {
        if (context is android.app.Activity) {
            return !context.isFinishing && !context.isDestroyed
        }
        return true
    }
}