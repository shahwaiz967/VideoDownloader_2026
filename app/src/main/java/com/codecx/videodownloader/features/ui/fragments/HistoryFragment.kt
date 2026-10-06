package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.FragmentHistoryBinding
import com.codecx.videodownloader.features.adapters.HistoryBookMarkAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.base.onBackPressWithAd
import com.codecx.videodownloader.features.ui.viewmodel.HistoryBookViewM
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()
    private val viewModel: HistoryBookViewM by viewModel()
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHistoryBinding {
        return FragmentHistoryBinding.inflate(inflater)
    }
    private val adapter = HistoryBookMarkAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = getString(R.string.history)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyList.collect { adapter.submitList(it) }
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        onBackPressWithAd(interstitialHelper) {
            safePopBackStack()
        }

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnClearAll.setOnClickListener {
            viewModel.clearHistory()
        }
        bindNative(
            RemoteConfigHelper.adIds.historyBanner,
            false,
            binding.adLayout
        )
    }


}