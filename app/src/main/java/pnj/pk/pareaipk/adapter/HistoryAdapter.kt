package pnj.pk.pareaipk.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.entity.HistoryEntity
import pnj.pk.pareaipk.databinding.ItemScanBinding

class HistoryAdapter(
    private val onDeleteClick: (HistoryEntity) -> Unit,
    private val onDetailClick: (HistoryEntity) -> Unit
) : ListAdapter<HistoryEntity, HistoryAdapter.ScanHistoryViewHolder>(ScanHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanHistoryViewHolder {
        val binding = ItemScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScanHistoryViewHolder(private val binding: ItemScanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scanHistory: HistoryEntity) {
            binding.apply {
                textTitle.text = scanHistory.class_label
                textScanDate.text = scanHistory.scanDate

                // Load image using Glide
                scanHistory.imageUri?.let { uri ->
                    Glide.with(itemView.context)
                        .load(uri)
                        .into(image)
                }

                buttonDelete.setOnClickListener {
                    showDeleteConfirmationDialog(scanHistory)
                }

                buttonDetailHistory.setOnClickListener {
                    onDetailClick(scanHistory)
                }
            }
        }

        private fun showDeleteConfirmationDialog(scanHistory: HistoryEntity) {
            AlertDialog.Builder(itemView.context)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.delete_confirmation_positive) { _, _ ->
                    // User confirmed, proceed with deletion
                    onDeleteClick(scanHistory)
                }
                .setNegativeButton(R.string.delete_confirmation_negative, null) // Do nothing if canceled
                .setIcon(R.drawable.ic_delete) // Optional: use your existing delete icon
                .show()
        }
    }

    private class ScanHistoryDiffCallback : DiffUtil.ItemCallback<HistoryEntity>() {
        override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}