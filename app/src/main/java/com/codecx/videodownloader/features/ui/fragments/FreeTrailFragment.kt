package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.R
import com.codecx.videodownloader.databinding.FragmentFreeTrailBinding
import com.codecx.videodownloader.features.base.BaseFragment


class FreeTrailFragment : BaseFragment<FragmentFreeTrailBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentFreeTrailBinding {
        return FragmentFreeTrailBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}