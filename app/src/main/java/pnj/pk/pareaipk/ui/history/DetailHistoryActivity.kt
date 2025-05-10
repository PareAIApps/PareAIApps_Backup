package pnj.pk.pareaipk.ui.history

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import pnj.pk.pareaipk.databinding.ActivityDetailHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class DetailHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailHistoryBinding
    private val detailViewModel: DetailHistoryViewModel by viewModels()

    companion object {
        const val EXTRA_SCAN_HISTORY_ID = "extra_scan_history_id"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide ActionBar
        supportActionBar?.hide()

        // Setup back navigation
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }

        // Get scan history ID from intent
        val extraScanHistoryId = intent.getIntExtra("extra_scan_history_id", -1).toLong()

        // Observe scan history details
        detailViewModel.loadScanHistoryById(extraScanHistoryId)
        detailViewModel.scanHistoryItem.observe(this, Observer { scanHistory ->
            scanHistory?.let {
                // Populate UI with scan history details
                binding.historyTitle.text = scanHistory.class_label

                // Parse confidence score from String to Float for ProgressBar
                val confidenceStr = scanHistory.confidenceScore.toString()
                val confidenceValue = try {
                    // Remove percent sign (%) if present
                    if (confidenceStr.contains("%")) {
                        confidenceStr.substring(0, confidenceStr.indexOf("%")).trim().toFloat()
                    } else {
                        confidenceStr.trim().toFloat()
                    }
                } catch (e: NumberFormatException) {
                    // Default to 0 if format is invalid
                    0f
                }

                // Set progress bar value - this will display our gradient color
                binding.confidenceProgressBar.progress = confidenceValue.roundToInt()

                // Set confidence text with percent sign
                val formattedConfidence = if (confidenceStr.contains("%")) {
                    // If percent sign already exists, use directly
                    confidenceStr
                } else {
                    // Format number without decimal if value is whole number
                    if (confidenceValue % 1 == 0f) {
                        "${confidenceValue.toInt()}%"  // Whole number without decimal
                    } else {
                        "$confidenceValue%"  // Number with decimal
                    }
                }
                binding.confidenceText.text = formattedConfidence

                binding.historyDescription.text = scanHistory.description
                binding.suggestionText.text = scanHistory.suggestion
                binding.receiptText.text = scanHistory.tools_receipt
                binding.tutorialText.text = scanHistory.tutorial

                // Format and set created at date
                val inputDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val outputDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

                val date = inputDateFormat.parse(scanHistory.scanDate)
                binding.createdAt.text = "Created at: ${outputDateFormat.format(date)}"

                // Load image using Glide
                Glide.with(this)
                    .load(scanHistory.imageUri)
                    .into(binding.historyImage)
            }
        })
    }
}