package pnj.pk.pareaipk.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.adapter.ArticleAdapter
import pnj.pk.pareaipk.adapter.HistoryHorizontalAdapter
import pnj.pk.pareaipk.data.response.ArticleResponse
import pnj.pk.pareaipk.data.retrofit.ApiConfig
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

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
                data = Uri.parse("mailto:pareaiproject@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Hubungi Kami")
            }
            startActivity(Intent.createChooser(emailIntent, "Kirim email ke kami"))
        }

        // Setup artikel dari API
        setupArticles()

        return root
    }

    private fun setupArticles() {
        // Inisialisasi adapter kosong terlebih dahulu
        articleAdapter = ArticleAdapter(emptyList()) { article ->
            // Handle article click - navigasi ke ArticleActivity
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

        // Ambil data artikel dari API
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.getArticles()

                if (response.isSuccessful) {
                    val articlesMap = response.body()
                    if (articlesMap != null) {
                        // Convert map values to list
                        val articlesList = articlesMap.values.toList()

                        // Update adapter dengan data dari API
                        articleAdapter = ArticleAdapter(articlesList) { article ->
                            // Handle article click - navigasi ke ArticleActivity
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
                } else {
                    // Handle error
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat artikel: " + response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                // Handle exception
                Toast.makeText(
                    requireContext(),
                    "Terjadi kesalahan: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace() // Log error for debugging
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}