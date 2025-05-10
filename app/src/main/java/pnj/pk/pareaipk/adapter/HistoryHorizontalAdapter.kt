package pnj.pk.pareaipk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.database.entity.HistoryEntity

class HistoryHorizontalAdapter(
    private val items: List<HistoryEntity>,
    private val onClick: (HistoryEntity) -> Unit
) : RecyclerView.Adapter<HistoryHorizontalAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textStory)
        private val image: ImageView = itemView.findViewById(R.id.imageStory)

        fun bind(item: HistoryEntity) {
            title.text = item.class_label
            Glide.with(itemView.context)
                .load(item.imageUri)
                .into(image)

            itemView.setOnClickListener {
                onClick(item) // Ini yang akan memanggil lambda untuk navigasi
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
