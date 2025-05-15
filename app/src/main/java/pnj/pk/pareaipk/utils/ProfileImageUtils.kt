package pnj.pk.pareaipk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Utility class for handling profile images with special focus on resizing
 * and optimizing images for small profile image views.
 */
object ProfileImageUtils {
    private const val TAG = "ProfileImageUtils"
    private const val PROFILE_IMAGE_SIZE = 200 // Target size in pixels for profile images
    private const val SMALL_PROFILE_SIZE = 300 // For very small profile pictures (e.g., in headers)
    private const val COMPRESSION_QUALITY = 100 // JPEG compression quality (0-100)

    /**
     * Safely loads a profile image from URI ensuring it fits within memory constraints
     * and is appropriately sized for display in small profile views (40dp)
     */
    fun loadProfileImageFromUri(context: Context, imageUri: Uri?): Bitmap? {
        if (imageUri == null) return null

        return try {
            // First attempt with efficient decoding options
            loadAndProcessBitmap(context, imageUri, SMALL_PROFILE_SIZE)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}", e)
            try {
                // Second attempt with more aggressive downsampling
                loadAndProcessBitmap(context, imageUri, SMALL_PROFILE_SIZE, true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed even with aggressive downsampling: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Load bitmap from URI with appropriate options for profile display
     */
    private fun loadAndProcessBitmap(
        context: Context,
        imageUri: Uri,
        targetSize: Int,
        aggressiveDownsample: Boolean = false
    ): Bitmap? {
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            // First, get dimensions without loading the full bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)

            // Calculate appropriate sample size
            var sampleSize = calculateInSampleSize(options, targetSize, targetSize)
            if (aggressiveDownsample) {
                sampleSize *= 2 // Even more aggressive downsampling
            }

            // Reset stream and decode with sample size
            inputStream.close()
            context.contentResolver.openInputStream(imageUri)?.use { resetStream ->
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
                }

                val bitmap = BitmapFactory.decodeStream(resetStream, null, decodeOptions)
                return bitmap?.let {
                    // Scale to exact size needed and ensure it's a square
                    cropToSquare(it)?.let { squared ->
                        scaleBitmap(squared, targetSize)
                    }
                }
            }
        }
        return null
    }

    /**
     * Calculate appropriate sample size for loading large bitmaps efficiently
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Crop bitmap to square shape (for profile pictures)
     */
    private fun cropToSquare(bitmap: Bitmap): Bitmap? {
        if (bitmap.width == bitmap.height) return bitmap

        val size = bitmap.width.coerceAtMost(bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        return try {
            Bitmap.createBitmap(bitmap, x, y, size, size)
        } catch (e: Exception) {
            Log.e(TAG, "Error cropping to square: ${e.message}", e)
            bitmap
        }
    }

    /**
     * Scale bitmap to target size
     */
    private fun scaleBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        return if (bitmap.width != targetSize || bitmap.height != targetSize) {
            try {
                Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
            } catch (e: Exception) {
                Log.e(TAG, "Error scaling bitmap: ${e.message}", e)
                bitmap
            }
        } else bitmap
    }

    /**
     * Save a bitmap to a temporary file that can be used with Glide
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")

            ByteArrayOutputStream().use { bos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, bos)
                FileOutputStream(file).use { fos ->
                    fos.write(bos.toByteArray())
                    fos.flush()
                }
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to file: ${e.message}", e)
            null
        }
    }

    /**
     * Process a file URI to ensure it's optimized for small profile displays
     */
    fun optimizeProfileImage(context: Context, uri: Uri?): Uri? {
        if (uri == null) return null

        try {
            val bitmap = loadProfileImageFromUri(context, uri)
            return bitmap?.let { saveBitmapToTempFile(context, it) } ?: uri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize profile image: ${e.message}", e)
            return uri // Return original if optimization fails
        }
    }
}