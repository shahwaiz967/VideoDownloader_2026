package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R

import com.codecx.videodownloader.databinding.FragmentHistoryBinding
import com.codecx.videodownloader.features.adapters.HistoryBookMarkAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.ui.viewmodel.HistoryBookViewM
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class BookMarkFragment : BaseFragment<FragmentHistoryBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHistoryBinding {
        return FragmentHistoryBinding.inflate(inflater)
    }

    private val viewModel: HistoryBookViewM by viewModel()
    private val adapter = HistoryBookMarkAdapter()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = getString(R.string.bookmark)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bookmarkList.collect { adapter.submitList(it) }
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
    }


}