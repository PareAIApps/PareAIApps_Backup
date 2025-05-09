package pnj.pk.pareaipk.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
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
            openLink("https://www.linkedin.com/in/fathirikhsan")
        }
        findViewById<ImageView>(R.id.iv_github_fathir).setOnClickListener {
            openLink("https://github.com/fathirikhsan")
        }
        findViewById<ImageView>(R.id.iv_instagram_fathir).setOnClickListener {
            openLink("https://www.instagram.com/fathirikhsan")
        }

        // Nabila Aulya Links
        findViewById<ImageView>(R.id.iv_linkedin_nabila).setOnClickListener {
            openLink("https://www.linkedin.com/in/nabila-aulya")
        }
        findViewById<ImageView>(R.id.iv_github_nabila).setOnClickListener {
            openLink("https://github.com/nabila-aulya")
        }
        findViewById<ImageView>(R.id.iv_instagram_nabila).setOnClickListener {
            openLink("https://www.instagram.com/nabila_aulya")
        }

        // Haidar Aditya Baran Links
        findViewById<ImageView>(R.id.iv_linkedin_haidar).setOnClickListener {
            openLink("https://www.linkedin.com/in/haidaraditya")
        }
        findViewById<ImageView>(R.id.iv_github_haidar).setOnClickListener {
            openLink("https://github.com/haidaraditya")
        }
        findViewById<ImageView>(R.id.iv_instagram_haidar).setOnClickListener {
            openLink("https://www.instagram.com/haidaraditya")
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
    }

    // Function to open URL in a browser
    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
