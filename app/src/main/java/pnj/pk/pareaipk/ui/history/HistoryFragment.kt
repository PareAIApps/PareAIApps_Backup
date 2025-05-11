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
import pnj.pk.pareaipk.adapter.HistoryFilterAdapter
import pnj.pk.pareaipk.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var scanHistoryAdapter: HistoryAdapter
    private lateinit var filterAdapter: HistoryFilterAdapter
    private val filterLabels = listOf(
        "Semua",
        "Bacterial Leaf Blight",
        "Brown Spot",
        "False Smut",
        "Healthy Plant",
        "Hispa",
        "Neck Blast",
        "Sheath Blight Rot",
        "Stemborer",
        "Wereng"
    )
    private var currentFilter = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterRecyclerView()
        setupHistoryRecyclerView()
        observeScanHistory()
    }

    private fun setupFilterRecyclerView() {
        filterAdapter = HistoryFilterAdapter(filterLabels) { selectedFilter ->
            currentFilter = selectedFilter
            filterHistoryItems()
        }

        binding.recyclerViewFilter.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = filterAdapter
        }
    }

    private fun setupHistoryRecyclerView() {
        scanHistoryAdapter = HistoryAdapter(
            onDeleteClick = { scanHistory ->
                // The confirmation dialog is now handled in the adapter
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
            // Store the full list in the ViewModel
            viewModel.setFullHistoryList(scanHistoryList)

            // Apply current filter
            filterHistoryItems()
        }
    }

    private fun filterHistoryItems() {
        val filteredList = if (currentFilter == "Semua") {
            viewModel.fullHistoryList.value ?: emptyList()
        } else {
            viewModel.fullHistoryList.value?.filter { it.class_label == currentFilter } ?: emptyList()
        }

        scanHistoryAdapter.submitList(filteredList)

        // Toggle empty state visibility
        binding.emptyStateLayout.visibility =
            if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}