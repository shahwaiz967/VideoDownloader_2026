package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codecx.videodownloader.databinding.FragmentSocityBinding
import com.codecx.videodownloader.features.base.BaseFragment


class SocietyFragment : BaseFragment<FragmentSocityBinding>() {


    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSocityBinding {
        return FragmentSocityBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headerText = arguments?.getString("name")

        binding.tvTitle.text = headerText
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


}