package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codecx.videodownloader.R
import com.codecx.videodownloader.databinding.FragmentHistoryBinding
import com.codecx.videodownloader.features.base.BaseFragment

class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHistoryBinding {
        return FragmentHistoryBinding.inflate(inflater)
    }


}