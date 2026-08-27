package com.codecx.videodownloader.features.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.models.DownloadApiResponse
import com.codecx.videodownloader.data.utils.RemoteConfigHelper

abstract class BaseFragment<viewBinding : ViewBinding> : Fragment() {
    protected lateinit var binding: viewBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = bindLayout(inflater, container, savedInstanceState)
        return binding.root
    }

    abstract fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): viewBinding

}

fun BaseFragment<*>.onBackPressWithAd(
    interstitialHelper: InterstitialAdHelper.InterstitialHelper,
    onBack: () -> Unit 
) {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showInter(
                    interstitialHelper = interstitialHelper,
                    stringId = RemoteConfigHelper.adIds.backPressInter.adId,
                    adAllowed = RemoteConfigHelper.adIds.backPressInter.isAdEnable,
                    onDismiss = {
                        isEnabled = false
                        onBack()
                    },
                    onFail = {
                        isEnabled = false
                        onBack()
                    }
                )
            }
        })
}
