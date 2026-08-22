package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.features.viewmodel.TabViewModel
import com.codecx.videodownloader.databinding.FragmentMainBinding
import com.codecx.videodownloader.features.adapters.TabAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : BaseFragment<FragmentMainBinding>(){

    private val tabViewModel: TabViewModel by viewModel()

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null){
            loadFragment(HomeFragment())
        }

        binding.tabCard.setOnClickListener {
            tabBottomSheet()
        }
       navigation()
    }



    private fun navigation(){
        binding.navHome.setOnClickListener {
            loadFragment(HomeFragment())
        }
        binding.navDownloads.setOnClickListener {
            loadFragment(DownloadFragment())
        }
        binding.navPlayer.setOnClickListener {
            loadFragment(PlayerFragment())
        }
        binding.navReels.setOnClickListener {
            safeNavigate(R.id.reelFragment)
        }
        binding.btnReels.setOnClickListener {
            safeNavigate(R.id.reelFragment)
        }
        binding.ivPremium.setOnClickListener {
            safeNavigate(R.id.premiumFragment)
        }
        binding.ivSettings.setOnClickListener {
            safeNavigate(R.id.settingFragment)
        }
    }


    private fun loadFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,fragment)
            .commit()
    }

    private fun tabBottomSheet() {

        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheet.setContentView(R.layout.tab_bottom_sheet)

        val backArrow = bottomSheet.findViewById<ImageView>(R.id.backButton)
        val btnAddTab = bottomSheet.findViewById<MaterialButton>(R.id.btnAddTab)
        val btnClearAll = bottomSheet.findViewById<TextView>(R.id.btnClearAll)
        val tvTabCount = bottomSheet.findViewById<TextView>(R.id.tvTabCount)
        val rvTabs = bottomSheet.findViewById<RecyclerView>(R.id.rvTabs)

        val adapter = TabAdapter(
            onTabClick = { tab ->
                tabViewModel.selectTab(tab.id)
                bottomSheet.dismiss()
                openTab(tab)
            },
            onCloseClick = { tab ->
                tabViewModel.removeTab(tab.id)
            }
        )

        rvTabs?.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            this.adapter = adapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                tabViewModel.tabs.collect { tabs ->
                    adapter.submitList(tabs)
                    tvTabCount?.text = "Tabs (${tabs.size})"
                    binding.ivTabBadge.text = tabs.size.toString()
                }
            }
        }

        backArrow?.setOnClickListener {
            bottomSheet.dismiss()
        }

        btnAddTab?.setOnClickListener {
            val newTab = tabViewModel.addTab()
            bottomSheet.dismiss()
            openTab(newTab)
        }

        btnClearAll?.setOnClickListener {
            tabViewModel.clearAll()
            bottomSheet.dismiss()
            val newTab = tabViewModel.addTab()
            openTab(newTab)
        }
        bottomSheet.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )
        bottomSheet.show()
    }

    private fun openTab(tab: TabModel) {

        loadFragment(
            HomeFragment.newInstance(
                tabId = tab.id
            )
        )
    }

}