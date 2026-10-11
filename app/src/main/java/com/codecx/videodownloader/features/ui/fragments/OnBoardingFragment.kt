package com.codecx.videodownloader.features.ui.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.utils.PrefManager
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentOnBoardingBinding
import com.codecx.videodownloader.features.adapters.onBoardingAdapter
import com.codecx.videodownloader.features.base.BaseFragment

class OnBoardingFragment : BaseFragment<FragmentOnBoardingBinding>() {
    private val adapter by lazy {
        onBoardingAdapter(listOf(R.drawable.onboarding_image1, R.drawable.onboarding_image2))
    }
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentOnBoardingBinding {
        return FragmentOnBoardingBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupClickListeners()
        bindNative(
            RemoteConfigHelper.adIds.onboardingTopNative,
            false,
            binding.topAdLayout
        )
        bindNative(
            RemoteConfigHelper.adIds.onboardingBottomNative,
            false,
            binding.bottomAdLayout
        )
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateText(position)
            }
        })
    }

    private fun updateText(position: Int) {
        when (position) {
            0 -> {
                binding.title.text = getString(R.string.fast_amp_easy_video_downloads)
                binding.des.text =
                    getString(R.string.save_videos_in_hd_quality_and_enjoy_seamless_offline_viewing_anytime_anywhere)
            }

            1 -> {
                binding.title.text = getString(R.string.quick_simple_and_reliable)
                binding.des.text =
                    getString(R.string.paste_any_video_link_and_save_your_favourite_videos_in_seconds)
            }
        }
    }

    private fun setupClickListeners() {
        binding.nextBtn.setOnClickListener {
            if (binding.viewPager.currentItem < adapter.itemCount - 1) {
                binding.viewPager.currentItem += 1
            } else {
                navigateToNext()
            }
        }

        binding.skipBtn.setOnClickListener {
            navigateToNext()
        }
    }

    private fun navigateToNext() {
        if (!isFragmentValid) return
        val complete = {
            PrefManager.setFirstRun(requireContext(), false)
            safeNavigate(R.id.languageFragment)
        }
        showInter(
            interstitialHelper,
            RemoteConfigHelper.adIds.onboardingFinishInter.adId,
            RemoteConfigHelper.adIds.onboardingTopNative.isAdEnable,
            onDismiss = complete,
            onFail = complete
        )

    }


}