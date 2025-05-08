package pnj.pk.pareaipk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pnj.pk.pareaipk.data.response.ArticleResponse
import pnj.pk.pareaipk.databinding.ItemArticleBinding

class ArticleAdapter(
    private val articles: List<ArticleResponse>,
    private val onItemClick: (ArticleResponse) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticleResponse) {
            binding.imageArticle.setImageResource(article.imageResId) // ID diperbaiki
            binding.textTitle.text = article.title
            binding.textDescription.text = article.description
            binding.textDate.text = article.date

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
