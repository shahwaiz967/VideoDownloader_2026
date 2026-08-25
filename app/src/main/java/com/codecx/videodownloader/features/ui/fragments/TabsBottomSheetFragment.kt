package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.features.adapters.TabAdapter
import com.codecx.videodownloader.features.ui.viewmodel.TabViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class TabsBottomSheetFragment(
    private val onTabSelected: (TabModel) -> Unit,
    private val onTabsChanged: () -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "TabsBottomSheet"
    }

    private val tabViewModel: TabViewModel by activityViewModels()

    private lateinit var adapter: TabAdapter
    private lateinit var tvTabCount: TextView
    private lateinit var rvTabs: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.tab_bottom_sheet,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        tvTabCount = view.findViewById(R.id.tvTabCount)
        rvTabs = view.findViewById(R.id.rvTabs)

        val btnClearAll =
            view.findViewById<TextView>(R.id.btnClearAll)

        val btnAddTab =
            view.findViewById<Button>(R.id.btnAddTab)

        setupRecyclerView()
        observeTabs()

        btnClearAll.setOnClickListener {
            tabViewModel.clearAll()
            onTabsChanged()
        }

        btnAddTab.setOnClickListener {

            val newTab = tabViewModel.addTab()

            onTabsChanged()

            onTabSelected(newTab)

            dismiss()

        }
    }

    private fun setupRecyclerView() {

        adapter = TabAdapter(
            mutableListOf(),

            onTabClick = { tab ->

                tabViewModel.selectTab(tab.id)
                dismiss()
            },

            onTabClose = { tab ->

                tabViewModel.removeTab(tab.id)

                onTabsChanged()
            }
        )

        rvTabs.layoutManager =
            GridLayoutManager(requireContext(), 2)

        rvTabs.adapter = adapter
    }

    private fun observeTabs() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                tabViewModel.tabs.collect { tabs ->

                    adapter.updateTabs(
                        tabs.toMutableList()
                    )

                    updateTabCount(tabs.size)
                }
            }
        }
    }

    private fun updateTabCount(count: Int) {

        tvTabCount.text =
            "$count ${if (count == 1) getString(R.string.tab) else getString(R.string.tabs)}"
    }
}