package com.codecx.videodownloader.features.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentMainBinding
import com.codecx.videodownloader.features.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton


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

        binding.tabCard.setOnClickListener {
            tabBottomSheet()
        }
       navigation()
    }

    private fun tabBottomSheet(){
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheet.setContentView(R.layout.tab_bottom_sheet)

        val backArrow = bottomSheet.findViewById<ImageView>(R.id.backButton)
        val btnAddTab = bottomSheet.findViewById<MaterialButton>(R.id.btnAddTab)

        backArrow?.setOnClickListener {
            bottomSheet.dismiss()
        }
        btnAddTab?.setOnClickListener {
            Toast.makeText(requireContext(), "Add Tab", Toast.LENGTH_SHORT).show()
        }
        bottomSheet.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bottomSheet.show()
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