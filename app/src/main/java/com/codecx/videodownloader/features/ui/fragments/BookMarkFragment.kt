package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.FragmentBookmarkBinding
import com.codecx.videodownloader.features.adapters.BookMarkAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.base.onBackPressWithAd
import com.codecx.videodownloader.features.ui.viewmodel.BookMarkViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class BookMarkFragment : BaseFragment<FragmentBookmarkBinding>() {
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentBookmarkBinding {
        return FragmentBookmarkBinding.inflate(inflater)
    }

    private val viewModel: BookMarkViewModel by viewModel()
    private val adapter = BookMarkAdapter()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = getString(R.string.bookmark)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bookmarkList.collect { adapter.submitList(it) }
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
            viewModel.clearBookmarks()
        }

        bindNative(
            RemoteConfigHelper.adIds.bookmarkBanner,
            false,
            binding.adLayout
        )
    }


}