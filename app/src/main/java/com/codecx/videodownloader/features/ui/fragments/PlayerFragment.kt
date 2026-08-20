package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.databinding.FragmentPlayerBinding
import com.codecx.videodownloader.features.adapters.PlayerChipAdapter
import com.codecx.videodownloader.features.base.BaseFragment

class PlayerFragment : BaseFragment<FragmentPlayerBinding>() {
    private lateinit var chipAdapter: PlayerChipAdapter

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPlayerBinding {
        return FragmentPlayerBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipList = listOf(
            "All","WhatsApp", "facebook", "Instagram",
            "TikTok", "SnapChat" , "Camera" , "Download"
        )
        binding.chipRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        chipAdapter = PlayerChipAdapter(chipList) { selectedChip ->
        }
        binding.chipRecycler.adapter = chipAdapter
    }


}