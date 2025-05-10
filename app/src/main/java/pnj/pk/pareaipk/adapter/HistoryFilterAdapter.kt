package pnj.pk.pareaipk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pnj.pk.pareaipk.databinding.ItemFilterHistoryBinding

class HistoryFilterAdapter(
    private val labels: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryFilterAdapter.FilterViewHolder>() {

    private var selectedPosition = 0 // "Semua" is selected by default

    inner class FilterViewHolder(private val binding: ItemFilterHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, position: Int) {
            binding.buttonLabel.text = item
            binding.buttonLabel.isSelected = position == selectedPosition

            binding.buttonLabel.setOnClickListener {
                if (selectedPosition != position) {
                    val oldPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                    onClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(labels[position], position)
    }

    override fun getItemCount() = labels.size
}