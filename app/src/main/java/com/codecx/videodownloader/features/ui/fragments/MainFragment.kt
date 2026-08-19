package com.codecx.videodownloader.features.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentMainBinding
import com.codecx.videodownloader.features.base.BaseFragment


class MainFragment : BaseFragment<FragmentMainBinding>(){



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


}