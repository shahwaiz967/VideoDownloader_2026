package com.codecx.videodownloader.features.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navOptions
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.utils.AppUpdateChecker
import com.codecx.videodownloader.data.utils.PrefManager
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentSplashBinding
import com.codecx.videodownloader.features.base.BaseFragment


class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSplashBinding {
        return FragmentSplashBinding.inflate(inflater)
    }

    private lateinit var appUpdate : AppUpdateChecker
    private var progress = 0
    private var progressComplete = false
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appUpdate = AppUpdateChecker(requireActivity() as AppCompatActivity)

        bindNative(
            RemoteConfigHelper.adIds.splashTopNative,
            false,
            binding.topAdLayout
        )
        bindNative(
            RemoteConfigHelper.adIds.splashBottomNative,
            false,
            binding.bottomAdLayout
        )

        if (activity?.intent?.action == Intent.ACTION_VIEW) {
            val uri = activity?.intent?.data
            if (uri != null) {
                safeNavigate(R.id.inAppBrowserFragment, args = Bundle().apply {
                    putString("url", uri.toString())
                }, navOptions = navOptions {
                    popUpTo(R.id.splashFragment) {
                        inclusive = true
                    }
                })
            }
        } else {
            appUpdate.checkAppUpdate {
                startProgressAnimation()
            }
        }
    }

    private fun startProgressAnimation() {

        val handler = Handler()
        val intervalMs = 6000L / 100

        val progressRunnable = object : Runnable {
            override fun run() {
                if (progress <= 100) {
                    binding.progressBar.progress = progress
                    binding.tvProgress.text = "$progress%"
                    progress++
                    handler.postDelayed(this, intervalMs)

                } else {
                    progressComplete = true
                    moveNext()
                }
            }

        }

        handler.post(progressRunnable)
    }

    private fun moveNext(){
        val destination = if (PrefManager.isFirstRun(requireContext())) {
            PrefManager.setFirstRun(requireContext(), false)
            R.id.languageFragment
        } else {
            R.id.premiumFragment
        }
        showInter(
            interstitialHelper = interstitialHelper,
            stringId = RemoteConfigHelper.adIds.splashInter.adId,
            adAllowed = RemoteConfigHelper.adIds.splashInter.isAdEnable,
            onDismiss = {
                safeNavigate(destination)
            },
            onFail = {
                safeNavigate(destination)
            }
        )
    }
    

}