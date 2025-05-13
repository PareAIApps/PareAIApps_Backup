package pnj.pk.pareaipk.utils
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import java.io.IOException
import java.io.InputStream


object ImageUser {

    fun resizeImage(context: Context, uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun resizeImageFromResource(context: Context, resId: Int, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, resId)
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false)
    }
}