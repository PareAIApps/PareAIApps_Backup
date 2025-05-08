package pnj.pk.pareaipk.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pnj.pk.pareaipk.adapter.HistoryAdapter
import pnj.pk.pareaipk.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var scanHistoryAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeScanHistory()
    }

    private fun setupRecyclerView() {
        scanHistoryAdapter = HistoryAdapter(
            onDeleteClick = { scanHistory ->
                viewModel.deleteScanHistory(scanHistory)
            },
            onDetailClick = { scanHistory ->
                val intent = Intent(requireContext(), DetailHistoryActivity::class.java).apply {
                    putExtra(DetailHistoryActivity.EXTRA_SCAN_HISTORY_ID, scanHistory.id)
                }
                startActivity(intent)
            }
        )

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun observeScanHistory() {
        viewModel.allScanHistory.observe(viewLifecycleOwner) { scanHistoryList ->
            scanHistoryAdapter.submitList(scanHistoryList)

            // Toggle empty state visibility
            binding.emptyStateLayout.visibility =
                if (scanHistoryList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
