package pnj.pk.pareaipk.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import com.google.android.material.appbar.MaterialToolbar
import pnj.pk.pareaipk.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

        // Set padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fathir Ikhsan Links
        findViewById<ImageView>(R.id.iv_linkedin_fathir).setOnClickListener {
            openLink("https://www.linkedin.com/in/fathir-ikhsan-292524223")
        }
        findViewById<ImageView>(R.id.iv_github_fathir).setOnClickListener {
            openLink("https://github.com/FireHR2004")
        }
        findViewById<ImageView>(R.id.iv_instagram_fathir).setOnClickListener {
            openLink("https://www.instagram.com/mufaik04_")
        }

        // Nabila Aulya Links
        findViewById<ImageView>(R.id.iv_linkedin_nabila).setOnClickListener {
            openLink("https://www.linkedin.com/in/nabila-aulya-fakhrunnisaa")
        }
        findViewById<ImageView>(R.id.iv_github_nabila).setOnClickListener {
            openLink("https://github.com/nabilaulyaf")
        }
        findViewById<ImageView>(R.id.iv_instagram_nabila).setOnClickListener {
            openLink("https://www.instagram.com/nabilaulyaf")
        }

        // Haidar Aditya Baran Links
        findViewById<ImageView>(R.id.iv_linkedin_haidar).setOnClickListener {
            openLink("https://www.linkedin.com/in/haidar-aditya-baran")
        }
        findViewById<ImageView>(R.id.iv_github_haidar).setOnClickListener {
            openLink("https://github.com/Githaidar")
        }
        findViewById<ImageView>(R.id.iv_instagram_haidar).setOnClickListener {
            openLink("https://www.instagram.com/haidarsecret/")
        }

        // Akhtar Faizi Putra Links
        findViewById<ImageView>(R.id.iv_linkedin_akhtar).setOnClickListener {
            openLink("https://www.linkedin.com/in/akhtarfaiziputra")
        }
        findViewById<ImageView>(R.id.iv_github_akhtar).setOnClickListener {
            openLink("https://github.com/akhtarerror")
        }
        findViewById<ImageView>(R.id.iv_instagram_akhtar).setOnClickListener {
            openLink("https://instagram.com/akhtarerror")
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

    }

    // Function to open URL in a browser
    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
