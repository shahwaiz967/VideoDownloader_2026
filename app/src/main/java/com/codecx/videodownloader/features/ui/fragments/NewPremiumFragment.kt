package com.codecx.videodownloader.features.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navOptions
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.models.PricesMenu
import com.codecx.videodownloader.data.utils.AndroidBillingHelper
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.AppConstants.INTRODUCTORY_ID
import com.codecx.videodownloader.data.utils.BillingHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.DialogTimerBinding
import com.codecx.videodownloader.databinding.FragmentNewPremiumBinding
import com.codecx.videodownloader.features.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import com.codecx.videodownloader.data.utils.beVisibleIf

class NewPremiumFragment : BaseFragment<FragmentNewPremiumBinding>() {
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()
    private var countDownTimer: CountDownTimer? = null
    private val billingHelper: BillingHelper by lazy {
        AndroidBillingHelper.getInstance(requireContext())
    }

    private enum class SelectedPlan {
        WEEKLY,
        MONTHLY,
        THREE_MONTHS
    }

    private var currentPlan = SelectedPlan.WEEKLY
    private var currentOffersList: List<PricesMenu> = emptyList()

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentNewPremiumBinding {
        return FragmentNewPremiumBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        selectPlan(SelectedPlan.WEEKLY)
        observeBillingOffers()

        val fromSettings = arguments?.getBoolean("fromSettings", false) ?: false

        binding.btnClose.setOnClickListener {
            if (fromSettings) {
                safePopBackStack()
            } else {
                showDialog()
            }
        }
    }

    private fun observeBillingOffers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingHelper.offersUpdateListener.collectLatest { offers ->
                    currentOffersList = offers
                    updatePricesFromOffers(offers)
                }
            }
        }
    }

    private fun updatePricesFromOffers(offers: List<PricesMenu>) {
        // --- Weekly Plan ---
        val weeklyFull =
            offers.find { it.productId == AppConstants.SUB_PREMIUM_WEEKLY && it.offerId == null }
        val weeklyDiscount =
            offers.find { it.productId == AppConstants.SUB_PREMIUM_WEEKLY && it.offerId == INTRODUCTORY_ID }

        if (weeklyDiscount != null && weeklyDiscount.price.isNotEmpty()) {
            binding.tvWeeklyPrice.text = weeklyDiscount.price
            if (weeklyFull != null && weeklyFull.price.isNotEmpty()) {
                binding.tvWeeklyOriginalPrice.text = weeklyFull.price
                binding.tvWeeklyOriginalPrice.visibility = View.VISIBLE
            } else {
                binding.tvWeeklyOriginalPrice.visibility = View.GONE
            }
            binding.tvWeeklyDiscount.visibility = View.VISIBLE
            binding.tvWeeklyDiscount.text =
                String.format(
                    "First %s %s then %s/week",
                    weeklyDiscount.formateBillingPeriod(),
                    weeklyDiscount.price,
                    weeklyFull?.price ?: ""
                )
//            if (weeklyFull != null && weeklyFull.microPrice > weeklyDiscount.microPrice) {
//                val pct = ((weeklyDiscount.microPrice - weeklyFull.microPrice).toDouble() / weeklyDiscount.microPrice * 100).toInt()
//                if (pct > 0) binding.tvWeeklyDiscount.text = "$pct% Off"
//            }
        } else {
            if (weeklyFull != null && weeklyFull.price.isNotEmpty()) {
                binding.tvWeeklyPrice.text = weeklyFull.price
            }
            binding.tvWeeklyOriginalPrice.visibility = View.GONE
            binding.tvWeeklyDiscount.visibility = View.VISIBLE
            binding.tvWeeklyDiscount.text = "Auto-Renewable"
        }

        // --- Monthly Plan ---
        val monthlyFull =
            offers.find { it.productId == AppConstants.SUB_PREMIUM_MONTHLY_DISCOUNT && it.offerId == null }
        val monthlyDiscount =
            offers.find { it.productId == AppConstants.SUB_PREMIUM_MONTHLY && it.offerId == null }

        if (monthlyDiscount != null && monthlyDiscount.price.isNotEmpty()) {
            binding.tvMonthlyPrice.text = monthlyDiscount.price
            if (monthlyFull != null && monthlyFull.price.isNotEmpty()) {
                binding.tvMonthlyOriginalPrice.text = monthlyFull.price
                binding.tvMonthlyOriginalPrice.visibility = View.VISIBLE
            } else {
                binding.tvMonthlyOriginalPrice.visibility = View.GONE
            }
            binding.tvMonthlyDiscount.visibility = View.VISIBLE
            if (monthlyFull != null && monthlyDiscount.microPrice > monthlyFull.microPrice) {
                val pct =
                    ((monthlyDiscount.microPrice - monthlyFull.microPrice).toDouble() / monthlyDiscount.microPrice * 100).toInt()
                if (pct > 0) binding.tvMonthlyDiscount.text = "$pct% Off"
            }
        } else {
            if (monthlyFull != null && monthlyFull.price.isNotEmpty()) {
                binding.tvMonthlyPrice.text = monthlyFull.price
            }
            binding.tvMonthlyOriginalPrice.visibility = View.GONE
            binding.tvMonthlyDiscount.visibility = View.GONE
        }

        val threeMonthsFull =
            offers.find { it.productId == AppConstants.SUB_PREMIUM_3_MONTHS_DISCOUNT && it.offerId == null }
        val threeMonthsDiscount =
            offers.find { it.productId == AppConstants.SUB_PREMIUM_3_MONTHS && it.offerId == null }

        if (threeMonthsDiscount != null && threeMonthsDiscount.price.isNotEmpty()) {
            binding.tvThreeMonthsPrice.text = threeMonthsDiscount.price
            if (threeMonthsFull != null && threeMonthsFull.price.isNotEmpty()) {
                binding.tvThreeMonthsOriginalPrice.text = threeMonthsFull.price
                binding.tvThreeMonthsOriginalPrice.visibility = View.VISIBLE
            } else {
                binding.tvThreeMonthsOriginalPrice.visibility = View.GONE
            }
            binding.tvThreeMonthsDiscount.visibility = View.VISIBLE
            if (threeMonthsFull != null && threeMonthsDiscount.microPrice > threeMonthsFull.microPrice) {
                val pct =
                    ((threeMonthsDiscount.microPrice - threeMonthsFull.microPrice).toDouble() / threeMonthsDiscount.microPrice * 100).toInt()
                if (pct > 0) binding.tvThreeMonthsDiscount.text = "$pct% Off"
            }
        } else {
            if (threeMonthsFull != null && threeMonthsFull.price.isNotEmpty()) {
                binding.tvThreeMonthsPrice.text = threeMonthsFull.price
            }
            binding.tvThreeMonthsOriginalPrice.visibility = View.GONE
            binding.tvThreeMonthsDiscount.visibility = View.GONE
        }
    }

    private fun showDialog() {
        val context = context ?: return
        val dialog = android.app.Dialog(context)
        val dialogBinding = DialogTimerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val scaleX =
            ObjectAnimator.ofFloat(dialogBinding.btnClaimOffer, View.SCALE_X, 1.0f, 1.10f).apply {
                duration = 600
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }
        val scaleY =
            ObjectAnimator.ofFloat(dialogBinding.btnClaimOffer, View.SCALE_Y, 1.0f, 1.10f).apply {
                duration = 600
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
            }

        val pulseAnimation = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }
        val offer =
            currentOffersList.find { it.productId == AppConstants.SUB_PREMIUM_SPECIAL_OFFER && it.offerId == INTRODUCTORY_ID }
        val full =
            currentOffersList.find { it.productId == AppConstants.SUB_PREMIUM_SPECIAL_OFFER && it.offerId == null }

        dialogBinding.offer50.beVisibleIf(offer != null)
        dialogBinding.btnClaimOffer.text = if (offer != null) "GET OFFER NOW" else "GET NOW"
        dialogBinding.priceText.text =
            if (offer != null) "First ${offer.formateBillingPeriod()} ${offer.price} then ${full?.price ?: ""}/${full?.formateBillingPeriod()}" else "${full?.price}/${full?.formateBillingPeriod()}"
        dialogBinding.btnClaimOffer.setOnClickListener {
            val p = offer ?: full
            if (p != null)
                billingHelper.launchBilling(p)
        }

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        // val width = resources.displayMetrics.widthPixels - resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val prefs = context.getSharedPreferences("TimerPrefs", android.content.Context.MODE_PRIVATE)
        var endTime = prefs.getLong("timer_end_time", 0L)
        val currentTime = System.currentTimeMillis()

        if (endTime <= currentTime) {
            endTime = currentTime + TimeUnit.HOURS.toMillis(24)
            prefs.edit { putLong("timer_end_time", endTime) }
        }

        val remainingMillis = endTime - currentTime

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                dialogBinding.tvHours.text = String.format(Locale.getDefault(), "%02d", hours)
                dialogBinding.tvMinutes.text = String.format(Locale.getDefault(), "%02d", minutes)
                dialogBinding.tvSeconds.text = String.format(Locale.getDefault(), "%02d", seconds)
            }

            override fun onFinish() {
                dialogBinding.tvHours.text = "00"
                dialogBinding.tvMinutes.text = "00"
                dialogBinding.tvSeconds.text = "00"
                prefs.edit().remove("timer_end_time").apply()
                dialog.dismiss()
            }
        }.start()

        dialog.setOnDismissListener {
            pulseAnimation.cancel()
            showInterstitial()
            countDownTimer?.cancel()
        }
    }


    private fun setupUI() {
        binding.tvWeeklyOriginalPrice.paintFlags =
            binding.tvWeeklyOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvMonthlyOriginalPrice.paintFlags =
            binding.tvMonthlyOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvThreeMonthsOriginalPrice.paintFlags =
            binding.tvThreeMonthsOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        binding.tvTermsLink.paintFlags =
            binding.tvTermsLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            safePopBackStack()
        }

        binding.cardWeekly.setOnClickListener {
            selectPlan(SelectedPlan.WEEKLY)
            binding.btnStartTrial.performClick()
        }

        binding.cardMonthly.setOnClickListener {
            selectPlan(SelectedPlan.MONTHLY)
            binding.btnStartTrial.performClick()

        }

        binding.cardThreeMonths.setOnClickListener {
            selectPlan(SelectedPlan.THREE_MONTHS)
            binding.btnStartTrial.performClick()
        }

        binding.btnStartTrial.setOnClickListener {
            launchSelectedPlanPurchase()
        }

        binding.tvTermsLink.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://appycreators12.blogspot.com/p/privacy-policy-for-video-downloader.html")
                )
            )
        }
    }

    private fun launchSelectedPlanPurchase() {
        val offer = when (currentPlan) {
            SelectedPlan.WEEKLY -> {
                currentOffersList.find { it.productId == AppConstants.SUB_PREMIUM_WEEKLY && it.offerId == INTRODUCTORY_ID }
                    ?: currentOffersList.find { it.productId == AppConstants.SUB_PREMIUM_WEEKLY && it.offerId == null }
            }

            SelectedPlan.MONTHLY -> {
                currentOffersList.find { it.productId == AppConstants.SUB_PREMIUM_MONTHLY && it.offerId == null }
            }

            SelectedPlan.THREE_MONTHS -> {
                currentOffersList.find { it.productId == AppConstants.SUB_PREMIUM_3_MONTHS && it.offerId == null }
            }
        }
        if (offer != null)
            billingHelper.launchBilling(offer)
    }

    private fun selectPlan(plan: SelectedPlan) {
        currentPlan = plan

        val selectedBg = R.drawable.bg_plan_selected
        val unselectedBg = R.drawable.bg_plan_unselected
        val selectedRb = R.drawable.ic_radio_selected
        val unselectedRb = R.drawable.ic_radio_unselected

        val pinkColor = 0xFFFF0055.toInt()
        val darkColor = 0xFF1A1A1A.toInt()

        when (plan) {
            SelectedPlan.WEEKLY -> {
                binding.layoutWeeklyInner.setBackgroundResource(selectedBg)
                binding.rbWeekly.setImageResource(selectedRb)
                binding.tvWeeklyPrice.setTextColor(pinkColor)

                binding.layoutMonthlyInner.setBackgroundResource(unselectedBg)
                binding.rbMonthly.setImageResource(unselectedRb)
                binding.tvMonthlyPrice.setTextColor(darkColor)

                binding.layoutThreeMonthsInner.setBackgroundResource(unselectedBg)
                binding.rbThreeMonths.setImageResource(unselectedRb)
                binding.tvThreeMonthsPrice.setTextColor(darkColor)
            }

            SelectedPlan.MONTHLY -> {
                binding.layoutWeeklyInner.setBackgroundResource(unselectedBg)
                binding.rbWeekly.setImageResource(unselectedRb)
                binding.tvWeeklyPrice.setTextColor(darkColor)

                binding.layoutMonthlyInner.setBackgroundResource(selectedBg)
                binding.rbMonthly.setImageResource(selectedRb)
                binding.tvMonthlyPrice.setTextColor(pinkColor)

                binding.layoutThreeMonthsInner.setBackgroundResource(unselectedBg)
                binding.rbThreeMonths.setImageResource(unselectedRb)
                binding.tvThreeMonthsPrice.setTextColor(darkColor)
            }

            SelectedPlan.THREE_MONTHS -> {
                binding.layoutWeeklyInner.setBackgroundResource(unselectedBg)
                binding.rbWeekly.setImageResource(unselectedRb)
                binding.tvWeeklyPrice.setTextColor(darkColor)

                binding.layoutMonthlyInner.setBackgroundResource(unselectedBg)
                binding.rbMonthly.setImageResource(unselectedRb)
                binding.tvMonthlyPrice.setTextColor(darkColor)

                binding.layoutThreeMonthsInner.setBackgroundResource(selectedBg)
                binding.rbThreeMonths.setImageResource(selectedRb)
                binding.tvThreeMonthsPrice.setTextColor(pinkColor)
            }
        }
    }

    private fun showInterstitial() {
        showInter(
            interstitialHelper = interstitialHelper,
            stringId = RemoteConfigHelper.adIds.onboardingFinishInter.adId,
            adAllowed = RemoteConfigHelper.adIds.onboardingFinishInter.isAdEnable,
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
