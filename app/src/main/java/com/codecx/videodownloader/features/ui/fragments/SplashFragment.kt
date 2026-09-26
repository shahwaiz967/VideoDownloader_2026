package com.codecx.videodownloader.features.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.openAd.OpenAppUtil
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.utils.AppUpdateChecker
import com.codecx.videodownloader.data.utils.PrefManager
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper.OPEN_AD_TYPE
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentSplashBinding
import com.codecx.videodownloader.features.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSplashBinding {
        return FragmentSplashBinding.inflate(inflater)
    }

    private lateinit var appUpdate: AppUpdateChecker
    private var progress = 0
    private var progressComplete = false
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appUpdate = AppUpdateChecker(requireActivity() as AppCompatActivity)

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
            binding.progressBar.isIndeterminate = true
            appUpdate.checkAppUpdate {
                lifecycleScope.launch {
                    RemoteConfigHelper.checkRemoteConfig {
                        loadAds()
                        startProgressAnimation()
                    }
                }
            }
        }
    }

    private fun loadAds() {
        bindNative(
            adItem = RemoteConfigHelper.adIds.splashTopNative,
            isPro = false,
            container = binding.topAdLayout
        )
        bindNative(
            adItem = RemoteConfigHelper.adIds.splashBottomNative,
            isPro = false,
            container = binding.bottomAdLayout
        )
    }

    private fun startProgressAnimation() {
        binding.progressBar.isIndeterminate = false
        binding.progressBar.max = 100

        lifecycleScope.launch {
            for (i in 0..100) {
                progress = i
                binding.progressBar.progress = i
                binding.tvProgress.text = "$i%"
                delay(30)
            }
            progressComplete = true
            binding.btnNext.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.tvProgress.visibility = View.GONE
            binding.btnNext.setOnClickListener {
                moveNext()
            }
        }
    }

    private fun moveNext() {
        val destination = if (PrefManager.isFirstRun(requireContext())) {
            R.id.onBoardingFragment
        } else {
            R.id.mainFragment
        }

        val adItem = RemoteConfigHelper.adIds.splashFullScreenAd
        if (adItem.isAdEnable) {
            if (adItem.adFormat == OPEN_AD_TYPE) {
                val openAdUtil = OpenAppUtil()
                openAdUtil.loadOpenAd(requireContext(), adId = adItem.adId, onAdLoaded = { ad ->
                    openAdUtil.showAd(
                        activity = requireActivity(),
                        ad = ad,
                        onAdShowed = {},
                        onAdDismiss = {
                            safeNavigate(destination)
                        },
                        onAdFail = {
                            safeNavigate(destination)
                        })
                }, onFailToLoad = {
                    safeNavigate(destination)
                })
            } else {
                showInter(
                    interstitialHelper = interstitialHelper,
                    stringId = adItem.adId,
                    adAllowed = true,
                    onDismiss = {
                        safeNavigate(destination)
                    },
                    onFail = {
                        safeNavigate(destination)
                    }
                )
            }
        } else {
            safeNavigate(destination)
        }
    }
}
