package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentPremiumBinding
import com.codecx.videodownloader.features.base.BaseFragment

class PremiumFragment : BaseFragment<FragmentPremiumBinding>() {


    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPremiumBinding {
        return FragmentPremiumBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStartTrial.setOnClickListener {
            safeNavigate(R.id.mainFragment,null, navOptions {
                popUpTo(R.id.nav_graph) {
                    inclusive = true
                }
            })
        }
    }
}