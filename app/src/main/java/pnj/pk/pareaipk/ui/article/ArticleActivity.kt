package pnj.pk.pareaipk.ui.article

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.ActivityArticleBinding

class ArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleBinding

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

        // Setup action bar
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Detail Artikel"

        // Get article data from intent
        val title = intent.getStringExtra(EXTRA_ARTICLE_TITLE) ?: "Artikel"
        val date = intent.getStringExtra(EXTRA_ARTICLE_DATE) ?: ""
        val description = intent.getStringExtra(EXTRA_ARTICLE_DESCRIPTION) ?: ""
        val imageUrl = intent.getStringExtra(EXTRA_ARTICLE_IMAGE) ?: ""

        // Set data to views
        // No longer using toolbar title, using ActionBar instead
        binding.textDetailTitle.text = title
        binding.textDetailDate.text = date
        binding.textDetailDescription.text = description

        // Load image with Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.logo)
            .error(R.drawable.logo)
            .into(binding.imageDetailArticle)

        // Setup share button
        binding.buttonShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\n\n$description\n\nDibagikan dari aplikasi PAREAI")
            }
            startActivity(Intent.createChooser(shareIntent, "Bagikan Artikel"))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}