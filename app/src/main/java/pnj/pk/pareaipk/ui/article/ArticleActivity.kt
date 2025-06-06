package pnj.pk.pareaipk.ui.article

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.ActivityArticleBinding
import java.io.ByteArrayOutputStream

class ArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleBinding
    private val viewModel: ArticleViewModel by viewModels()

    companion object {
        const val EXTRA_ARTICLE_TITLE = "extra_article_title"
        const val EXTRA_ARTICLE_DATE = "extra_article_date"
        const val EXTRA_ARTICLE_DESCRIPTION = "extra_article_description"
        const val EXTRA_ARTICLE_IMAGE = "extra_article_image"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan ActionBar bawaan
        supportActionBar?.hide()

        // Setup toolbar dengan back button
        setupToolbar()

        // Get article data from intent
        val title = intent.getStringExtra(EXTRA_ARTICLE_TITLE) ?: getString(R.string.title_article)
        val date = intent.getStringExtra(EXTRA_ARTICLE_DATE) ?: ""
        val description = intent.getStringExtra(EXTRA_ARTICLE_DESCRIPTION) ?: getString(R.string.description_article)
        val imageUrl = intent.getStringExtra(EXTRA_ARTICLE_IMAGE) ?: ""

        // Set data to ViewModel
        viewModel.setArticleData(title, date, description, imageUrl)

        // Setup observers
        setupObservers()

        // Setup share button
        binding.buttonShare.setOnClickListener {
            shareArticleWithImage()
        }
    }

    private fun setupToolbar() {
        // Tidak perlu memanggil setSupportActionBar(binding.toolbar) karena ini menyebabkan konflik
        binding.toolbarTitle.text = getString(R.string.detail_article)

        // Mengatur fungsi navigasi back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed() // Kembali ke halaman sebelumnya
        }
    }

    private fun setupObservers() {
        // Observe title
        viewModel.title.observe(this) { title ->
            binding.textDetailTitle.text = title
        }

        // Observe date
        viewModel.date.observe(this) { date ->
            binding.textDetailDate.text = date
        }

        // Observe description
        viewModel.description.observe(this) { description ->
            binding.textDetailDescription.text = description
        }

        // Observe imageUrl
        viewModel.imageUrl.observe(this) { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(binding.imageDetailArticle)
        }

        // Optional: Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            // You could add a progress indicator if needed
        }

        // Optional: Observe error messages
        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareArticleWithImage() {
        val imageUrl = viewModel.getImageUrlForShare()

        if (imageUrl.isNotEmpty()) {
            // Load image from URL and share with text
            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        shareImageAndText(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle case where image loading is cleared (e.g., activity destroyed)
                        // Do nothing, as we don't want to share text only in this specific scenario
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        // Image loading failed, show a toast or log the error, but do not share text only
                        Toast.makeText(this@ArticleActivity, "Gagal memuat gambar untuk dibagikan.", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            // Share only text if no image URL
            shareTextOnly()
        }
    }

    private fun shareImageAndText(bitmap: Bitmap) {
        try {
            // Save bitmap to MediaStore and get URI
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "Article Image",
                null
            )
            val imageUri = Uri.parse(path)

            // Create share intent with both image and text
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, viewModel.title.value)
                putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)))
        } catch (e: Exception) {
            // If image sharing fails, show a toast or log the error.
            // Do not fall back to text only, as the user intended to share with an image.
            Toast.makeText(this, "Gagal membagikan gambar artikel.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareTextOnly() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, viewModel.title.value)
            putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)))
    }

    // Function ini masih bisa digunakan jika user memilih opsi back dari menu (meskipun tidak terlihat)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}