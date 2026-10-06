package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        observeBillingOffers()
    }

    private fun observeBillingOffers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingHelper.offersUpdateListener.collectLatest { offers ->
                    val trialOffer = offers.find {
                        it.productId == AppConstants.SUB_PREMIUM_WEEKLY_3DAY_TRIAL
                    }
                    val regularOffer = offers.find {
                        it.productId == AppConstants.SUB_PREMIUM_WEEKLY
                    }

                    if (trialOffer != null && trialOffer.price.isNotEmpty()) {
                        // Trial Available
                        selectedPlan = trialOffer
                        binding.headerPrice.text = regularOffer?.price ?: ""
                        binding.tvHeaderSubtitle.text = "Try all PRO features free for 3 Days."
                        binding.headerTitle.text = "3 Day Free Trial"
                        binding.tvBadge.visibility = View.VISIBLE
                        binding.tvSubtitle.text = "Starting today  •  Cancel anytime"
                        binding.tvBtnText.text = "Start 3-Days Free Trial"
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
}
