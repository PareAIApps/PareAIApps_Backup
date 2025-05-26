package pnj.pk.pareaipk.ui.article

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pnj.pk.pareaipk.adapter.ArticleAdapter
import pnj.pk.pareaipk.databinding.FragmentArticleBinding
import pnj.pk.pareaipk.ui.home.HomeViewModel

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        val root = binding.root
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Setup articles
        setupArticles()

        return root
    }

    private fun setupArticles() {
        // Initialize empty adapter
        articleAdapter = ArticleAdapter(emptyList()) { article ->
            val intent = Intent(requireContext(), ArticleActivity::class.java).apply {
                putExtra(ArticleActivity.EXTRA_ARTICLE_TITLE, article.label)
                putExtra(ArticleActivity.EXTRA_ARTICLE_DATE, article.createdAt)
                putExtra(ArticleActivity.EXTRA_ARTICLE_DESCRIPTION, article.description)
                putExtra(ArticleActivity.EXTRA_ARTICLE_IMAGE, article.imageUrl)
            }
            startActivity(intent)
        }

        binding.recyclerViewArticle.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }

        // Observe articles from ViewModel
        homeViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articleAdapter = ArticleAdapter(articles) { article ->
                val intent = Intent(requireContext(), ArticleActivity::class.java).apply {
                    putExtra(ArticleActivity.EXTRA_ARTICLE_TITLE, article.label)
                    putExtra(ArticleActivity.EXTRA_ARTICLE_DATE, article.createdAt)
                    putExtra(ArticleActivity.EXTRA_ARTICLE_DESCRIPTION, article.description)
                    putExtra(ArticleActivity.EXTRA_ARTICLE_IMAGE, article.imageUrl)
                }
                startActivity(intent)
            }
            binding.recyclerViewArticle.adapter = articleAdapter
        }

        // Observe loading state
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can show/hide a progress indicator here if needed
            if (isLoading) {
                // Show loading indicator
            } else {
                // Hide loading indicator
            }
        }

        // Observe error messages
        homeViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch articles
        homeViewModel.fetchArticles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}