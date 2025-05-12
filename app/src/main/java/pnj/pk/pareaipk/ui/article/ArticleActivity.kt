package pnj.pk.pareaipk.ui.article

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.ActivityArticleBinding

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
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, viewModel.title.value)
                putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)))
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Menyembunyikan judul default

        // Menetapkan judul di TextView kustom
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

    // Function ini masih bisa digunakan jika user memilih opsi back dari menu (meskipun tidak terlihat)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}