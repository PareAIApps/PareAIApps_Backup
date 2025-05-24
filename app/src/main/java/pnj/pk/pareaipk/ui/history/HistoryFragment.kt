package pnj.pk.pareaipk.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.adapter.HistoryAdapter
import pnj.pk.pareaipk.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var scanHistoryAdapter: HistoryAdapter

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
    private var isSelectionMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterButton()
        setupHistoryRecyclerView()
        setupSelectionToggle()
        setupActionButtons()
        setupSelectAllFunctionality()
        observeScanHistory()
    }

    private fun setupFilterButton() {
        // Set initial text
        binding.filterButton.text = currentFilter

        binding.filterButton.setOnClickListener { view ->
            showFilterPopup(view)
        }
    }

    private fun setupSelectionToggle() {
        binding.topRightLabel.setOnClickListener {
            if (!isSelectionMode) {
                toggleSelectionMode()
            } else {
                // When in selection mode, "Pilih Semua" acts as select all
                toggleSelectAll()
            }
        }
    }

    private fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode

        if (isSelectionMode) {
            // Switch to selection mode
            binding.topRightLabel.text = "Pilih Semua"
            binding.historyCheckBox.visibility = View.VISIBLE
            binding.leftCheckBox.visibility = View.VISIBLE
            binding.actionButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.VISIBLE

            // Hide filter elements
            binding.titleTextView.visibility = View.GONE
            binding.filterButton.visibility = View.GONE

            // Remove margin end when in selection mode
            val layoutParams = binding.topRightLabel.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.marginEnd = 0
            binding.topRightLabel.layoutParams = layoutParams
        } else {
            // Switch back to normal mode
            binding.topRightLabel.text = "Pilih Riwayat"
            binding.historyCheckBox.visibility = View.GONE
            binding.leftCheckBox.visibility = View.GONE
            binding.actionButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE

            // Show filter elements
            binding.titleTextView.visibility = View.VISIBLE
            binding.filterButton.visibility = View.VISIBLE

            // Reset checkbox states
            binding.historyCheckBox.isChecked = false
            binding.leftCheckBox.isChecked = false

            // Restore margin end when back to normal mode
            val layoutParams = binding.topRightLabel.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.marginEnd = (16 * resources.displayMetrics.density).toInt()
            binding.topRightLabel.layoutParams = layoutParams
        }
    }

    private fun setupSelectAllFunctionality() {
        // Handle select all checkbox
        binding.historyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.leftCheckBox.isChecked = isChecked
            // Here you can also update the adapter to select/deselect all items
            // scanHistoryAdapter.selectAll(isChecked)
        }

        // Handle left checkbox
        binding.leftCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.historyCheckBox.isChecked = isChecked
            // Here you can also update the adapter to select/deselect all items
            // scanHistoryAdapter.selectAll(isChecked)
        }
    }

    private fun toggleSelectAll() {
        val newState = !binding.historyCheckBox.isChecked
        binding.historyCheckBox.isChecked = newState
        binding.leftCheckBox.isChecked = newState
        // Here you can also update the adapter to select/deselect all items
        // scanHistoryAdapter.selectAll(newState)
    }

    private fun showFilterPopup(anchorView: View) {
        val popupMenu = PopupMenu(requireContext(), anchorView, Gravity.END)

        // Add menu items dynamically
        filterLabels.forEachIndexed { index, label ->
            popupMenu.menu.add(0, index, 0, label)
        }

        // Set checked item (optional - for visual indication)
        val currentIndex = filterLabels.indexOf(currentFilter)
        if (currentIndex != -1) {
            popupMenu.menu.findItem(currentIndex)?.isChecked = true
        }

        // Adjust popup position to align with button
        try {
            val popupField = PopupMenu::class.java.getDeclaredField("mPopup")
            popupField.isAccessible = true
            val menuPopupHelper = popupField.get(popupMenu)
            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
            val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
            setForceIcons.invoke(menuPopupHelper, true)

            // Set horizontal offset to align better with button
            val setHorizontalOffset = classPopupHelper.getMethod("setHorizontalOffset", Int::class.javaPrimitiveType)
            setHorizontalOffset.invoke(menuPopupHelper, -20) // Adjust this value as needed

        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val selectedFilter = filterLabels[menuItem.itemId]
            if (selectedFilter != currentFilter) {
                currentFilter = selectedFilter
                binding.filterButton.text = currentFilter
                filterHistoryItems()
            }
            true
        }

        popupMenu.show()
    }

    private fun setupActionButtons() {
        // Delete button
        binding.actionButton.setOnClickListener {
            // Handle delete selected items
            // You can implement the delete logic here
            // For example: deleteSelectedItems()
        }

        // Cancel button
        binding.cancelButton.setOnClickListener {
            toggleSelectionMode() // Exit selection mode
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