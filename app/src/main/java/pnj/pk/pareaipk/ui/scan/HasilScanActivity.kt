package pnj.pk.pareaipk.ui.scan

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pnj.pk.pareaipk.MainActivity
import pnj.pk.pareaipk.databinding.ActivityHasilScanBinding
import pnj.pk.pareaipk.utils.uriToFile
import java.text.SimpleDateFormat
import java.util.*

class HasilScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHasilScanBinding
    private val viewModel: HasilScanViewModel by viewModels()

    companion object {
        const val KEY_IMG_URI = "img_uri_key"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHasilScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }

        val imageUriString = intent.getStringExtra(KEY_IMG_URI)
        if (imageUriString == null) {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imageUri = Uri.parse(imageUriString)
        val imageFile = uriToFile(imageUri, this)
        binding.historyImage.setImageURI(imageUri)

        // Show progress bar initially
        binding.progressBar.visibility = View.VISIBLE
        binding.historyContainer.visibility = View.GONE
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        viewModel.predictImage(imageFile)

        viewModel.predictionResult.observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            binding.historyContainer.visibility = View.VISIBLE

            result.onSuccess { mlResponse ->
                binding.historyTitle.text = mlResponse.class_label
                val confidencePercent = (mlResponse.confidence * 100).toInt()
                binding.confidenceText.text = " %.2f%%".format(mlResponse.confidence * 100)
                binding.confidenceProgressBar.progress = confidencePercent

                binding.historyDescription.text = mlResponse.description.ifEmpty { "No description." }
                binding.suggestionText.text = mlResponse.suggestion.ifEmpty { "No suggestion." }
                binding.receiptText.text = mlResponse.tools_receipt.ifEmpty { "No receipt." }
                binding.tutorialText.text = mlResponse.tutorial.ifEmpty { "No tutorial." }

                val createdAt = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                binding.createdAt.text = "Created at: $createdAt"
            }

            result.onFailure { exception ->
                Toast.makeText(this, "Prediction failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                binding.historyTitle.text = "Prediction Error"
                binding.historyDescription.text = exception.message ?: "Unknown error occurred"
            }
        }

        // 🚨 AlertDialog untuk "Objek Tidak Tersedia." dan navigasi ke ScanFragment
        viewModel.popupMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Peringatan")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        // Kirim intent ke MainActivity untuk membuka ScanFragment
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("navigateToScan", true)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                    .show()
            }
        }
    }
}
