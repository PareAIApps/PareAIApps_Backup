package pnj.pk.pareaipk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pnj.pk.pareaipk.databinding.ItemFilterHistoryBinding

class HistoryFilterAdapter(
    private val labels: List<String>, // Change to List<String>
    private val onClick: (String) -> Unit // Update the click listener to accept String
) : RecyclerView.Adapter<HistoryFilterAdapter.LabelViewHolder>() {

    inner class LabelViewHolder(private val binding: ItemFilterHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) { // Bind a String instead of ModelMLResponse
            binding.buttonLabel.text = item
            binding.buttonLabel.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val binding = ItemFilterHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LabelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(labels[position]) // Pass String
    }

    override fun getItemCount() = labels.size
}
