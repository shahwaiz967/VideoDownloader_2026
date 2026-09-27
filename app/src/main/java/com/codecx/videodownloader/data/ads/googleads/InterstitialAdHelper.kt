package com.codecx.videodownloader.data.ads.googleads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.webkit.WebView
import androidx.annotation.LayoutRes
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback

import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.GDPR.UMPConsent
import com.codecx.videodownloader.data.ads.interfaces.AdCallBack
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig
import com.codecx.videodownloader.data.ads.utils.LoadingUtils
import com.codecx.videodownloader.data.ads.utils.isNetworkConnected
import com.codecx.videodownloader.data.ads.utils.runOnUiThread
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.showToast
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds


object InterstitialAdHelper {
    var isInterstitialShowing: Boolean = false
    var isInterstitialLoading: Boolean = false

    fun checkIfTimePassed(lastAdTimer: Long, timerMilliSec: Long): Boolean {
        if (lastAdTimer == 0L) {
            Log.d("Ad Timer", "Timer has not been started yet.")
            return true
        }

        val currentTime = SystemClock.elapsedRealtime()
        val timeElapsed = currentTime - lastAdTimer
        if (timeElapsed >= timerMilliSec) {
            return true
            Log.d("Ad Timer", "Time passed: $timeElapsed ms (Timer complete)")
        } else {
            return false
            Log.d("Ad Timer", "Time remaining: ${timerMilliSec - timeElapsed} ms")
        }

        return false
    }

    class InterstitialHelper {
        var isTimerComplete = false
        var lastAdTimer: Long = 0L
        private var interstitialAd: InterstitialAd? = null

        fun adIsAvailable() = interstitialAd != null

        fun getAd() = interstitialAd

        fun destroyAd() {
            interstitialAd = null
        }

        fun loadInterstitial(
            context: Context,
            adId: String,
            onAdLoaded: (InterstitialAd) -> Unit,
            onFailToLoad: () -> Unit
        ) {
            if (context.isNetworkConnected() && WebView.getCurrentWebViewPackage() != null && CodecxAd.isSDKInit) {
                val interAdCallBack = object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        super.onAdLoaded(ad)
                        runOnUiThread {
                            interstitialAd = ad
                            onAdLoaded(ad)
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        runOnUiThread {
                            onFailToLoad()
                        }
                    }
                }
                InterstitialAd.load(
                    AdRequest.Builder(adId).build(), interAdCallBack
                )
            } else {
                onFailToLoad()
            }
        }

        fun showInterstitial(
            activity: Activity,
            ad: InterstitialAd?,
            onAdDismissed: () -> Unit,
            onAdShowed: () -> Unit = {},
            onAdFail: () -> Unit = {},
        ) {
            if (ad == null) {
                LoadingUtils.dismissScreen()
                onAdFail()
            } else {
                ad.show(activity)
                ad.adEventCallback =
                    object : InterstitialAdEventCallback {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            interstitialAd = null
                            isInterstitialShowing = false
                            runOnUiThread {
                                onAdDismissed()
                            }
                        }

                        override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                            super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                            interstitialAd = null
                            isInterstitialShowing = false
                            runOnUiThread {
                                onAdFail()
                            }
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            if (CodecxAd.getAdConfig()?.isDisableResumeAdOnClick == true) {
                                OpenAdConfig.isOpenAdStop = true
                            }
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            isInterstitialShowing = true
                            runOnUiThread {
                                onAdShowed()
                            }
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            FirebaseAnalytics.getInstance(activity).logEvent(
                                FirebaseAnalytics.Event.AD_IMPRESSION, Bundle().apply {
                                    putString(FirebaseAnalytics.Param.AD_FORMAT, "interstitial")
                                    putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                                    putString(FirebaseAnalytics.Param.AD_UNIT_NAME, "interstitial")
                                }
                            )
                        }

                        override fun onAdPaid(value: AdValue) {
                            super.onAdPaid(value)
                            FirebaseAnalytics.getInstance(activity).logEvent(
                                FirebaseAnalytics.Event.ADD_PAYMENT_INFO,
                                Bundle().apply {
                                    putString(FirebaseAnalytics.Param.CURRENCY, value.currencyCode)
                                    putLong(FirebaseAnalytics.Param.VALUE, value.valueMicros)
                                    putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                                    putString(FirebaseAnalytics.Param.AD_UNIT_NAME, "interstitial")
                                }
                            )
                        }
                    }
            }


        }

        fun loadShowInterstitial(
            adId: String?,
            adAllowed: Boolean,
            showLoadingLayout: Boolean = true,
            timerAllowed: Boolean = false,
            timerMilliSec: Long = 0L,
            @LayoutRes
            loadingLayout: Int = R.layout.dialog_ad_loading,
            adCallBack: AdCallBack, activity: Activity
        ) = CoroutineScope(Dispatchers.Main).launch {
            isTimerComplete = if (!timerAllowed) {
                true
            } else {
                checkIfTimePassed(lastAdTimer, timerMilliSec)
            }

            if (WebView.getCurrentWebViewPackage() != null && CodecxAd.isSDKInit) {
                if (adAllowed && UMPConsent.isUMPAllowed && CodecxAd.getAdConfig()?.isGoogleAdsAllowed == true) {
                    if (isTimerComplete) {
                        if (activity.isNetworkConnected()) {
                            if (showLoadingLayout) {
                                LoadingUtils.showAdLoadingScreen(
                                    activity,
                                    layoutId = loadingLayout
                                )
                            }
                            isInterstitialLoading = true
                            fun requestToShowAd(p0: InterstitialAd) {
                                isInterstitialLoading = false
                                adCallBack.onAdLoaded()
                                p0.show(activity)
                                p0.adEventCallback =
                                    object : InterstitialAdEventCallback {
                                        override fun onAdDismissedFullScreenContent() {
                                            super.onAdDismissedFullScreenContent()
                                            isInterstitialShowing = false
                                            if (timerAllowed) {
                                                lastAdTimer = SystemClock.elapsedRealtime()
                                            }
                                            runOnUiThread {
                                                adCallBack.onAdDismiss()
                                               // LoadingUtils.dismissScreen()
                                            }


                                        }



                                        override fun onAdFailedToShowFullScreenContent(
                                            fullScreenContentError: FullScreenContentError
                                        ) {
                                            super.onAdFailedToShowFullScreenContent(
                                                fullScreenContentError
                                            )
                                            isInterstitialShowing = false
                                            runOnUiThread {

                                                LoadingUtils.dismissScreen()
                                                adCallBack.onAdFailToShow(
                                                    Exception(
                                                        fullScreenContentError.message
                                                    )
                                                )
                                            }
                                        }

                                        override fun onAdClicked() {
                                            super.onAdClicked()
                                            if (CodecxAd.getAdConfig()?.isDisableResumeAdOnClick == true) {
                                                OpenAdConfig.isOpenAdStop = true
                                            }
                                            runOnUiThread {
                                                adCallBack.onAdClick()
                                            }
                                        }

                                        override fun onAdShowedFullScreenContent() {
                                            super.onAdShowedFullScreenContent()
                                            isInterstitialShowing = true
                                            runOnUiThread {
                                                LoadingUtils.dismissScreen()
                                                adCallBack.onAdShown()
                                            }

                                        }

                                        override fun onAdImpression() {
                                            super.onAdImpression()
                                            FirebaseAnalytics.getInstance(activity).logEvent(
                                                FirebaseAnalytics.Event.AD_IMPRESSION, Bundle().apply {
                                                    putString(FirebaseAnalytics.Param.AD_FORMAT,"interstitial")
                                                    putString(FirebaseAnalytics.Param.AD_PLATFORM,"admob")
                                                    putString(FirebaseAnalytics.Param.AD_UNIT_NAME,adId?:"interstitial")
                                                }
                                            )
                                        }

                                        override fun onAdPaid(value: AdValue) {
                                            super.onAdPaid(value)
                                            FirebaseAnalytics.getInstance(activity).logEvent(
                                                FirebaseAnalytics.Event.ADD_PAYMENT_INFO,
                                                Bundle().apply {
                                                    putString(FirebaseAnalytics.Param.CURRENCY, value.currencyCode)
                                                    putLong(FirebaseAnalytics.Param.VALUE, value.valueMicros)
                                                    putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                                                    putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adId ?: "interstitial")
                                                }
                                            )
                                        }


                                    }
                            }

                            fun failToLoadAd() {
                                LoadingUtils.dismissScreen()
                                isInterstitialLoading = false
                                adCallBack.onAdFailToLoad(Exception("Fail to load"))
                            }

                            withTimeoutOrNull(
                                (CodecxAd.getAdConfig()?.adsTimeOut ?: 10000L).milliseconds
                            ) {
                                getInterAd(
                                    activity,
                                    adId ?: "ca-app-pub-3940256099942544/1033173712"
                                )
                            }?.let { ad ->
                                requestToShowAd(ad)
                            } ?: run {
                                failToLoadAd()
                            }

                        } else {
                            adCallBack.onAdFailToLoad(Exception("No internet found"))
                        }
                    } else {
                        adCallBack.onAdFailToLoad(TimeDelayException())
                    }
                } else {
                    adCallBack.onAdFailToLoad(Exception("Ad not allowed"))
                }
            } else {
                adCallBack.onAdFailToLoad(Exception("Ad not allowed"))
            }

        }

        private suspend fun getInterAd(activity: Activity, adId: String): InterstitialAd? {
            return suspendCancellableCoroutine {
                val interAdCallBack = object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        super.onAdLoaded(ad)
                        runOnUiThread {
                            it.resume(ad)
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        runOnUiThread {
                            it.resume(null)
                        }
                    }
                }
                InterstitialAd.load(
                    AdRequest.Builder(adId).build(), interAdCallBack
                )
            }
        }

    }

    class TimeDelayException : Exception() {
        private fun readResolve(): Any = TimeDelayException()
    }


}