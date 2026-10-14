package com.codecx.videodownloader.features.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.codecx.videodownloader.data.models.PricesMenu
import com.codecx.videodownloader.data.utils.AndroidBillingHelper
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.BillingHelper
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.FragmentFreeTrailBinding
import com.codecx.videodownloader.features.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FreeTrailFragment : BaseFragment<FragmentFreeTrailBinding>() {

    private val billingHelper: BillingHelper by lazy {
        AndroidBillingHelper.getInstance(requireContext())
    }

    private var selectedPlan: PricesMenu? = null

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentFreeTrailBinding {
        return FragmentFreeTrailBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            safePopBackStack()
        }

        binding.start3DaysTrailBtn.setOnClickListener {
            launchPurchase()
        }

        binding.cardFreeTrial.setOnClickListener {
            launchPurchase()
        }

        binding.restorePurchase.setOnClickListener {
            lifecycleScope.launch {
                val isRestored = billingHelper.restorePurchases()
                if (isRestored) {
                    Toast.makeText(requireContext(), "Purchases restored successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No active subscriptions found.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.textTermsAndCondition.setOnClickListener {
            privacyLink()
        }

        observeBillingOffers()
    }

    private fun observeBillingOffers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingHelper.offersUpdateListener.collectLatest { offers ->
                    val trialOffer = offers.find {
                        it.productId == AppConstants.SUB_PREMIUM_WEEKLY_3DAY_TRIAL && it.offerId == AppConstants.INTRODUCTORY_ID
                    }
                    val regularOffer = offers.find {
                        it.productId == AppConstants.SUB_PREMIUM_WEEKLY_3DAY_TRIAL && it.offerId == null
                    }

                    if (trialOffer != null && trialOffer.price.isNotEmpty()) {
                        // Trial Available
                        selectedPlan = trialOffer
                        binding.headerPrice.text = regularOffer?.price ?: ""
                        binding.tvHeaderSubtitle.text =
                            "Try all PRO features free for ${trialOffer.formateBillingPeriod()}."
                        binding.headerTitle.text = "${trialOffer.formateBillingPeriod()} Free Trial"
                        binding.tvBadge.visibility = View.VISIBLE
                        binding.tvSubtitle.text = "Starting today  •  Cancel anytime"
                        binding.tvBtnText.text =
                            "Start ${trialOffer.formateBillingPeriod()} Free Trial"
                    } else if (regularOffer != null && regularOffer.price.isNotEmpty()) {
                        // Trial NOT Available - Simple Billing Info
                        selectedPlan = regularOffer
                        binding.headerPrice.text = regularOffer.price
                        binding.tvHeaderSubtitle.text = "Get full access to all PRO features."
                        binding.headerTitle.text = "Weekly Premium"
                        binding.tvBadge.visibility = View.GONE
                        binding.tvSubtitle.text = "Billed weekly  •  Cancel anytime"
                        binding.tvBtnText.text = "Subscribe Now"
                    } else {
                        // Fallback default
                        selectedPlan = PricesMenu(
                            price = "",
                            microPrice = 0L,
                            currencyCode = "",
                            productId = AppConstants.SUB_PREMIUM_WEEKLY_3DAY_TRIAL
                        )
                    }
                }
            }
        }
    }

    private fun launchPurchase() {
        val plan = selectedPlan ?: PricesMenu(
            price = "",
            microPrice = 0L,
            currencyCode = "",
            productId = AppConstants.SUB_PREMIUM_WEEKLY_3DAY_TRIAL
        )
        billingHelper.launchBilling(plan)
    }

    private fun privacyLink() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://appycreators12.blogspot.com/p/privacy-policy-for-video-downloader.html")
            )
        )
    }
}
