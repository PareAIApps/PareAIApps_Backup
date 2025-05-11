package pnj.pk.pareaipk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.data.response.ArticleResponse
import pnj.pk.pareaipk.databinding.ItemArticleBinding

class ArticleAdapter(
    private val articles: List<ArticleResponse>,
    private val onItemClick: (ArticleResponse) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleResponse) {
            // Load image using Glide from imageUrl
            Glide.with(binding.root.context)
                .load(article.imageUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(binding.imageArticle)

            // Set text data from ArticleResponse
            binding.textTitle.text = article.label
            binding.textDescription.text = article.subtitle
            binding.textDate.text = article.createdAt

            // Article click listener
            binding.root.setOnClickListener {
                onItemClick(article)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}