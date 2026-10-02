package com.codecx.videodownloader.features.ui.fragments

import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.utils.AndroidBillingHelper
import com.codecx.videodownloader.data.utils.BillingHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.DialogTimerBinding
import com.codecx.videodownloader.databinding.FragmentNewPremiumBinding
import com.codecx.videodownloader.features.base.BaseFragment
import java.util.Locale
import java.util.concurrent.TimeUnit

class NewPremiumFragment : BaseFragment<FragmentNewPremiumBinding>() {
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()
    private var countDownTimer: CountDownTimer? = null
    private val billingHelper : BillingHelper by lazy {
        AndroidBillingHelper.getInstance(requireContext())
    }

    private enum class SelectedPlan {
        WEEKLY,
        MONTHLY,
        THREE_MONTHS
    }

    private var currentPlan = SelectedPlan.WEEKLY

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
        val fromSettings = arguments?.getBoolean("fromSettings", false) ?: false

        binding.btnClose.setOnClickListener {
            if (fromSettings) {
                findNavController().popBackStack()
            } else {
                showDialog()

            }
        }
    }

    private fun showDialog() {
        val context = requireContext()
        val dialog = android.app.Dialog(context)
        val dialogBinding = DialogTimerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        val width = resources.displayMetrics.widthPixels -
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp)

        dialog.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )


        val prefs = context.getSharedPreferences("TimerPrefs", android.content.Context.MODE_PRIVATE)
        var endTime = prefs.getLong("timer_end_time", 0L)
        val currentTime = System.currentTimeMillis()

        if (endTime <= currentTime) {
            endTime = currentTime + TimeUnit.HOURS.toMillis(24)
            prefs.edit().putLong("timer_end_time", endTime).apply()
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
            findNavController().popBackStack()
        }

        binding.cardWeekly.setOnClickListener {
            selectPlan(SelectedPlan.WEEKLY)
        }

        binding.cardMonthly.setOnClickListener {
            selectPlan(SelectedPlan.MONTHLY)
        }

        binding.cardThreeMonths.setOnClickListener {
            selectPlan(SelectedPlan.THREE_MONTHS)
        }

        binding.btnStartTrial.setOnClickListener {
            val planName = when (currentPlan) {
                SelectedPlan.WEEKLY -> "Weekly"
                SelectedPlan.MONTHLY -> "Monthly"
                SelectedPlan.THREE_MONTHS -> "3 Months"
            }
            Toast.makeText(
                requireContext(),
                getString(R.string.trial_started) + " ($planName)",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.tvTermsLink.setOnClickListener {
            Toast.makeText(requireContext(), "Terms & Conditions", Toast.LENGTH_SHORT).show()
        }
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
