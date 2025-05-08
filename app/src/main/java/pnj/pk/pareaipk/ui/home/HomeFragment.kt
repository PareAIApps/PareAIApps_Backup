package pnj.pk.pareaipk.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.adapter.ArticleAdapter
import pnj.pk.pareaipk.adapter.HistoryHorizontalAdapter
import pnj.pk.pareaipk.data.response.ArticleResponse
import pnj.pk.pareaipk.databinding.FragmentHomeBinding
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

        // Dummy artikel
        val articleList = listOf(
            ArticleResponse("Penyakit Daun", "Cara mengenali dan mengatasi penyakit daun.", "5 Mei 2024", R.drawable.ic_launcher_background),
            ArticleResponse("Hama Tikus", "Tips mengusir hama tikus dari sawah.", "3 Mei 2024", R.drawable.ic_launcher_background),
            ArticleResponse("Pemupukan Efektif", "Waktu dan teknik pemupukan yang tepat.", "1 Mei 2024", R.drawable.ic_launcher_background)
        )

        articleAdapter = ArticleAdapter(articleList) { article ->
            Toast.makeText(requireContext(), "Klik artikel: ${article.title}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewArticle.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
