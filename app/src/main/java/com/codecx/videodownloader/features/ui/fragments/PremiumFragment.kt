package com.codecx.videodownloader.features.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.codecx.videodownloader.BuildConfig
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.models.PricesMenu
import com.codecx.videodownloader.data.models.PurchaseEvents
import com.codecx.videodownloader.data.utils.AndroidBillingHelper
import com.codecx.videodownloader.data.utils.BillingHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentPremiumBinding
import com.codecx.videodownloader.features.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PremiumFragment : BaseFragment<FragmentPremiumBinding>() {

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentPremiumBinding {
        return FragmentPremiumBinding.inflate(inflater, container, false)

    }

    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()

    private val billing : BillingHelper by lazy {
        AndroidBillingHelper.getInstance(requireContext())
    }
    private var selectedPlan : PricesMenu? = null
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

        binding.btnStartTrial.setOnClickListener {
            val currentPlan = selectedPlan ?: return@setOnClickListener
            billing.launchBilling(currentPlan)
        }
        binding.swTrial.setOnCheckedChangeListener { _, _ ->
            updateUi(
                forceCheck = false,
                offers = billing.offersUpdateListener.value
            )
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    billing.purchaseUpdateEventListener.collect { event ->
                        when (event) {
                            PurchaseEvents.PurchaseDone -> {
                            }

                            else  -> {
                            }
                        }
                    }
                }
                launch {
                    billing.offersUpdateListener.collectLatest { offers ->
                        updateUi(
                            forceCheck = true,
                            offers = offers
                        )
                    }
                }
            }
        }
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

    @SuppressLint("StringFormatInvalid")
    private fun updateUi(
        forceCheck: Boolean = false,
        offers: List<PricesMenu>
    ) {

        val trialPlan =
            offers.find { plan ->

                plan.productId == BuildConfig.weeklyPlanId &&
                        plan.id == BuildConfig.weeklyBasePlanId &&
                        plan.offerId == BuildConfig.weeklyFreeTrailId
            }
        val solidPlan =
            offers.find { plan ->

                plan.productId == BuildConfig.weeklyPlanId &&
                        plan.id == BuildConfig.weeklyBasePlanId &&
                        plan.offerId == null
            }




        if (forceCheck) {
            binding.swTrial.isChecked = true
        }

        binding.cvTrial.visibility =
            if (trialPlan != null) {
                VISIBLE
            } else {
                GONE
            }

        val currentPlan =
            if (binding.swTrial.isChecked && trialPlan != null) {
                trialPlan
            } else {
                solidPlan
            }
        selectedPlan = currentPlan
        with(binding) {

            tvWeeklyPrice.text =
                currentPlan?.price ?: ""

            val isOnTrial =
                currentPlan?.offerId ==
                        BuildConfig.weeklyFreeTrailId

            if (isOnTrial) {
                tvNoPayment1.text =
                    getString(R.string.no_payment_now)

                btnStartTrial.text = getString(R.string.continue_for_free)

                tvNoPayment1.visibility =
                    VISIBLE

                tvPremiumSubtitle.text =
                    getString(
                        R.string.premium_subtitle,
                        solidPlan?.price ?: ""
                    )

            } else {
                tvNoPayment1.text =
                    getString(R.string.trial_disabled)


                tvNoPayment1.visibility =
                    GONE

                btnStartTrial.text = getString(R.string.buy_now)

            }

            btnStartTrial.isEnabled =
                currentPlan != null
        }

    }

}