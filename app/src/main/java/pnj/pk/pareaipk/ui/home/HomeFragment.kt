package pnj.pk.pareaipk.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.adapter.ArticleAdapter
import pnj.pk.pareaipk.adapter.HistoryHorizontalAdapter
import pnj.pk.pareaipk.databinding.FragmentHomeBinding
import pnj.pk.pareaipk.ui.article.ArticleActivity
import pnj.pk.pareaipk.ui.chatbot.ChatbotActivity
import pnj.pk.pareaipk.ui.history.DetailHistoryActivity
import pnj.pk.pareaipk.ui.history.HistoryViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyHorizontalAdapter: HistoryHorizontalAdapter
    private lateinit var articleAdapter: ArticleAdapter
    private val historyViewModel: HistoryViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels() // Add HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Setup histori
        historyHorizontalAdapter = HistoryHorizontalAdapter(emptyList()) { scanHistory ->
            val intent = Intent(requireContext(), DetailHistoryActivity::class.java).apply {
                putExtra(DetailHistoryActivity.EXTRA_SCAN_HISTORY_ID, scanHistory.id)
            }
            startActivity(intent)
        }

        binding.recyclerViewHistoryHorizontal.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = historyHorizontalAdapter
        }

        historyViewModel.allScanHistory.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNullOrEmpty()) {
                binding.historyHeaderLayout.visibility = View.GONE
                binding.recyclerViewHistoryHorizontal.visibility = View.GONE
            } else {
                binding.historyHeaderLayout.visibility = View.VISIBLE
                binding.recyclerViewHistoryHorizontal.visibility = View.VISIBLE

                val top5History = historyList.take(5)
                historyHorizontalAdapter = HistoryHorizontalAdapter(top5History) { scanHistory ->
                    val intent = Intent(requireContext(), DetailHistoryActivity::class.java).apply {
                        putExtra(DetailHistoryActivity.EXTRA_SCAN_HISTORY_ID, scanHistory.id)
                    }
                    startActivity(intent)
                }
                binding.recyclerViewHistoryHorizontal.adapter = historyHorizontalAdapter
            }
        }

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.navigation_home, true)
            .build()

        binding.textSeeAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_history, null, navOptions)
        }

        binding.buttonChat.setOnClickListener {
            val intent = Intent(requireContext(), ChatbotActivity::class.java)
            startActivity(intent)
        }

        // Setup Hubungi Kami click listener
        binding.card2.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${getString(R.string.contact_email)}")
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_us_subject))
            }
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email_to_us)))
        }

        // Setup articles using ViewModel
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