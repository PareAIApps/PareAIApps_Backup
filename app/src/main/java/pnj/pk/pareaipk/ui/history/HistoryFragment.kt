package pnj.pk.pareaipk.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
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

    // Use getString(R.string.all_filter) for "Semua"
    private val filterLabels by lazy {
        listOf(
            getString(R.string.all), // Changed to string resource
            "Bacterial Leaf Blight",
            "Bacterial Leaf Streak",
            "Bacterial Panicle Blight",
            "Blast",
            "Brown Spot",
            "Dead Heart",
            "Downy Mildew",
            "False Smut",
            "Healty",
            "Hispa",
            "Tungro"
        )
    }
    private var currentFilter: String = "" // Initialize later after context is available
    private var isSelectionMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize currentFilter here, after context is available
        currentFilter = getString(R.string.all) // Initialize with the string resource

        // Initially hide the fixedHeaderContainer
        binding.fixedHeaderContainer.visibility = View.GONE

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
        binding.fabDelete.setOnClickListener {
            if (!isSelectionMode) {
                toggleSelectionMode()
            } else {
                // When in selection mode, FAB acts as select all
                toggleSelectAll()
            }
        }
    }

    private fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode

        if (isSelectionMode) {
            // Switch to selection mode
            binding.fixedHeaderContainer.visibility = View.VISIBLE // Make the header visible
            binding.historyCheckBox.visibility = View.VISIBLE
            binding.actionButton.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.VISIBLE
            binding.selectedCountTextView.visibility = View.VISIBLE
            binding.fabDelete.visibility = View.GONE // **Hide FAB Delete**
            binding.historyTitleTextView.visibility = View.GONE

            // Hide filter elements
            binding.filterButton.visibility = View.GONE

            // Enable selection mode in adapter
            scanHistoryAdapter.setSelectionMode(true)

            // Update selected count display
            updateSelectedCountDisplay(0)
        } else {
            // Switch back to normal mode
            binding.fixedHeaderContainer.visibility = View.GONE // Hide the header
            binding.historyCheckBox.visibility = View.GONE
            binding.actionButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
            binding.selectedCountTextView.visibility = View.GONE
            binding.fabDelete.visibility = View.VISIBLE // **Show FAB Delete**
            binding.historyTitleTextView.visibility = View.VISIBLE

            // Show filter elements
            binding.filterButton.visibility = View.VISIBLE

            // Reset checkbox states
            binding.historyCheckBox.isChecked = false

            // Disable selection mode in adapter
            scanHistoryAdapter.setSelectionMode(false)
        }
    }

    private fun setupSelectAllFunctionality() {
        // Handle select all checkbox
        binding.historyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            scanHistoryAdapter.selectAll(isChecked)
        }
    }

    private fun toggleSelectAll() {
        val newState = !scanHistoryAdapter.areAllItemsSelected()
        binding.historyCheckBox.isChecked = newState
        scanHistoryAdapter.selectAll(newState)
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
            val selectedItems = scanHistoryAdapter.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                showDeleteSelectedConfirmationDialog(selectedItems)
            }
        }

        // Cancel button
        binding.cancelButton.setOnClickListener {
            toggleSelectionMode() // Exit selection mode
        }
    }

    private fun showDeleteSelectedConfirmationDialog(selectedItems: List<pnj.pk.pareaipk.database.entity.HistoryEntity>) {
        val message = if (selectedItems.size == 1) {
            getString(R.string.delete_one_item_confirmation)
        } else {
            getString(R.string.delete_multiple_items_confirmation, selectedItems.size)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteSelectedItems(selectedItems)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .setIcon(R.drawable.ic_trash)
            .create()

        dialog.setOnShowListener {
            // Ubah warna tombol "Delete" (positive button) menjadi merah
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColor(R.color.red)) // pastikan warna merah ada di colors.xml
        }
        dialog.show()
    }

    private fun deleteSelectedItems(selectedItems: List<pnj.pk.pareaipk.database.entity.HistoryEntity>) {
        selectedItems.forEach { item ->
            viewModel.deleteScanHistory(item)
        }
        // Exit selection mode after deletion
        toggleSelectionMode()
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

        // Set up selection change listener
        scanHistoryAdapter.setOnSelectionChangedListener { hasSelection ->
            // Update the main checkbox based on adapter selection state
            if (isSelectionMode) {
                binding.historyCheckBox.isChecked = scanHistoryAdapter.areAllItemsSelected()

                // Update selected count display
                val selectedCount = scanHistoryAdapter.getSelectedItems().size
                updateSelectedCountDisplay(selectedCount)
            }
        }

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun updateSelectedCountDisplay(count: Int) {
        val text = if (count == 1) {
            binding.root.context.getString(R.string.item_selected_single, count)
        } else {
            binding.root.context.getString(R.string.item_selected_single, count)
        }
        binding.selectedCountTextView.text = text
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
        val filteredList = if (currentFilter == getString(R.string.all)) { // Changed to string resource
            viewModel.fullHistoryList.value ?: emptyList()
        } else {
            viewModel.fullHistoryList.value?.filter { it.class_label == currentFilter } ?: emptyList()
        }

        scanHistoryAdapter.submitList(filteredList)

        // Toggle empty state visibility and FAB visibility
        if (filteredList.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.fabDelete.visibility = View.GONE // Hide FAB when no history
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            // Only show FAB if not in selection mode
            if (!isSelectionMode) {
                binding.fabDelete.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}