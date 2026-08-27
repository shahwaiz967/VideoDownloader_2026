package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
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

    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fromSettings = arguments?.getBoolean("fromSettings", false) ?: false

        binding.btnBack.setOnClickListener{
            if (fromSettings){
                findNavController().popBackStack()
            }else{
                showInterstitial()
            }
        }
        binding.ebayReview.tvReviewText.text = getString(R.string.review_ebay_text)
        binding.ebayReview.tvReviewerName.text = getString(R.string.review_ebay_name)

        binding.emilyReview.tvReviewText.text = getString(R.string.review_emily_text)
        binding.emilyReview.tvReviewerName.text = getString(R.string.review_emily_name)

        binding.noahReview.tvReviewText.text = getString(R.string.review_noah_text)
        binding.noahReview.tvReviewerName.text = getString(R.string.review_noah_name)

        itemCardRow()

    }

    private fun itemCardRow(){
        binding.rowDownloadManager.tvFeatureName.text = getString(R.string.download_manager)
        binding.rowDownloadManager.ivBasicCheck.setImageResource(R.drawable.ic_check_orange)
        binding.rowUnlimitedAccess.tvFeatureName.text = getString(R.string.unlimited_access)
        binding.rowUnlimitedAccess.ivBasicCheck.setImageResource(R.drawable.cross)
        binding.rowFastDownloads.tvFeatureName.text = getString(R.string.fast_downloads)
        binding.rowFastDownloads.ivBasicCheck.setImageResource(R.drawable.cross)
        binding.rowAdFree.tvFeatureName.text = getString(R.string._100_ad_free)
        binding.rowAdFree.ivBasicCheck.setImageResource(R.drawable.cross)
        binding.rowPrioritySupport.tvFeatureName.text = getString(R.string.priority_support)
        binding.rowPrioritySupport.ivBasicCheck.setImageResource(R.drawable.cross)
    }

    private fun showInterstitial(){
        showInter(
            interstitialHelper = interstitialHelper,
            stringId = RemoteConfigHelper.adIds.splashInter.adId,
            adAllowed = RemoteConfigHelper.adIds.splashInter.isAdEnable,
            onDismiss = {
                safeNavigate(R.id.mainFragment, null, navOptions {
                    popUpTo(R.id.nav_graph) {
                        inclusive = true
                    }
                })
            },
            onFail = {
                safeNavigate(R.id.mainFragment, null, navOptions {
                    popUpTo(R.id.nav_graph) {
                        inclusive = true
                    }
                })
            }
        )
    }

}