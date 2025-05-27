package pnj.pk.pareaipk.ui.article

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.adapter.ArticleAdapter
import pnj.pk.pareaipk.databinding.FragmentArticleBinding
import pnj.pk.pareaipk.ui.home.HomeViewModel

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private val homeViewModel: HomeViewModel by viewModels()
    private var refreshAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        val root = binding.root
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Setup articles
        setupArticles()
        setupRefreshButton()

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
            updateUI(articles, isLoading = false)

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
            if (isLoading) {
                updateUI(emptyList(), isLoading = true)
            }
        }

        // Observe error messages
        homeViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                // Show empty state when there's an error
                updateUI(emptyList(), isLoading = false, hasError = true)
            }
        }

        // Fetch articles
        homeViewModel.fetchArticles()
    }

    private fun setupRefreshButton() {
        binding.fabRefresh.setOnClickListener {
            startRefreshAnimation()
            homeViewModel.fetchArticles()
        }
    }

    private fun updateUI(articles: List<Any>, isLoading: Boolean, hasError: Boolean = false) {
        when {
            isLoading -> {
                // Show loading state
                binding.recyclerViewArticle.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.loadingStateLayout.visibility = View.VISIBLE
                stopRefreshAnimation()
            }
            articles.isEmpty() -> {
                // Show empty state
                binding.recyclerViewArticle.visibility = View.GONE
                binding.loadingStateLayout.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
                stopRefreshAnimation()

                // Update empty state message based on error
                if (hasError) {
                    binding.emptyStateTitle.text = getString(R.string.error_loading_articles)
                    binding.emptyStateDescription.text = getString(R.string.error_loading_description)
                } else {
                    binding.emptyStateTitle.text = getString(R.string.refresh_title_articles)
                    binding.emptyStateDescription.text = getString(R.string.refresh_description)
                }
            }
            else -> {
                // Show articles
                binding.loadingStateLayout.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.recyclerViewArticle.visibility = View.VISIBLE
                stopRefreshAnimation()
            }
        }
    }

    private fun startRefreshAnimation() {
        stopRefreshAnimation()
        refreshAnimator = ObjectAnimator.ofFloat(binding.fabRefresh, "rotation", 0f, 360f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopRefreshAnimation() {
        refreshAnimator?.let { animator ->
            animator.cancel()
            binding.fabRefresh.rotation = 0f
        }
        refreshAnimator = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopRefreshAnimation()
        _binding = null
    }
}