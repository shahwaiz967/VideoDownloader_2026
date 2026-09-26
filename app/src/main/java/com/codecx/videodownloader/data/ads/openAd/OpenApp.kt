package com.codecx.videodownloader.data.ads.openAd


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.LayoutRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.GDPR.UMPConsent
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig.isOpenAdLoading
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig.isOpenAdShowing
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig.isOpenAdStop
import com.codecx.videodownloader.data.ads.utils.LoadingUtils
import com.codecx.videodownloader.data.ads.utils.isNetworkConnected
import com.codecx.videodownloader.data.ads.utils.runOnUiThread
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.google.firebase.analytics.FirebaseAnalytics

class OpenAppUtil {
    fun loadOpenAd(
        context: Context,
        adId: String,
        onAdLoaded: (AppOpenAd) -> Unit,
        onFailToLoad: () -> Unit
    ) {
        if (CodecxAd.isSDKInit) {
            val loadCallback = object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    runOnUiThread {
                        isOpenAdLoading = false
                        onAdLoaded(ad)
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    runOnUiThread {
                        isOpenAdLoading = false
                        onFailToLoad()
                    }
                }
            }
            AppOpenAd.load(
                adRequest = AdRequest.Builder(adUnitId = adId).build(),
                loadCallback
            )
        } else {
            onFailToLoad()
        }
    }

    fun showAd(
        activity: Activity,
        ad: AppOpenAd,
        onAdShowed: () -> Unit,
        onAdDismiss: () -> Unit,
        onAdFail: () -> Unit
    ) {
        val fullScreenContentCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                isOpenAdShowing = false
                runOnUiThread {
                    onAdDismiss()
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                isOpenAdShowing = false
                runOnUiThread {
                    LoadingUtils.dismissScreen()
                    onAdFail()
                }
            }

            override fun onAdShowedFullScreenContent() {
                isOpenAdShowing = true
                runOnUiThread {
                    onAdShowed()
                }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                FirebaseAnalytics.getInstance(activity).logEvent(
                    FirebaseAnalytics.Event.AD_IMPRESSION, Bundle().apply {
                        putString(FirebaseAnalytics.Param.AD_FORMAT, "app_open")
                        putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                        putString(FirebaseAnalytics.Param.AD_UNIT_NAME, "app_open")
                    }
                )
            }

        }
        ad.adEventCallback = fullScreenContentCallback
        ad.show(activity)
    }
}


class OpenApp(
    private val application: Application,
    private val isLoadingViewVisible: Boolean,
    //@LayoutRes
    // private val loadingLayout: Int = R.layout.openad_loading_layout
) :
    Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var lastAdTimer: Long = 0L

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                onAppForegrounded()
            }

            override fun onStop(owner: LifecycleOwner) {

            }

        })
    }


    private var isFirstLaunch = true

    private fun fetchAd() {
        currentActivity?.let {
            if (it.isNetworkConnected()) {
                if (!InterstitialAdHelper.isInterstitialLoading && !InterstitialAdHelper.isInterstitialShowing && !isOpenAdLoading && !isOpenAdShowing) {

                    val adConfig = RemoteConfigHelper.adIds.resumeAppOpen

                    if (!adConfig.isAdEnable) return

                    isOpenAdLoading = true
                    /* if (isLoadingViewVisible) {
                         Handler(Looper.getMainLooper()).postDelayed({
                             LoadingUtils.showAdLoadingScreen(it, loadingLayout)
                         }, 200L)
                     }*/

                    val loadCallback: AdLoadCallback<AppOpenAd> =
                        object : AdLoadCallback<AppOpenAd> {
                            override fun onAdLoaded(ad: AppOpenAd) {
                                runOnUiThread {
                                    isOpenAdLoading = false
                                    appOpenAd = ad
                                    showAdIfAvailable()
                                    isFirstLaunch = false
                                }
                            }


                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                runOnUiThread {
                                    isOpenAdLoading = false
                                    LoadingUtils.dismissScreen()
                                }
                            }
                        }
                    AppOpenAd.load(
                        adRequest = AdRequest.Builder(adConfig.adId)
                            .build(), loadCallback
                    )
                }
            }

        }
    }


    private fun showAdIfAvailable() {
        if (isAdAvailable()) {
            val fullScreenContentCallback = object : AppOpenAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    LoadingUtils.dismissScreen()
                    isOpenAdShowing = false
                    lastAdTimer = SystemClock.elapsedRealtime()

                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                    isOpenAdShowing = false
                    LoadingUtils.dismissScreen()
                }


                override fun onAdShowedFullScreenContent() {
                    isOpenAdShowing = true
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    currentActivity?.let { activity ->
                        FirebaseAnalytics.getInstance(activity).logEvent(
                            FirebaseAnalytics.Event.AD_IMPRESSION, Bundle().apply {
                                putString(FirebaseAnalytics.Param.AD_FORMAT, "app_open")
                                putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                                putString(FirebaseAnalytics.Param.AD_UNIT_NAME, RemoteConfigHelper.adIds.resumeAppOpen.adId)
                            }
                        )
                    }
                }
            }
            appOpenAd?.adEventCallback = fullScreenContentCallback
            currentActivity?.let {
                try {
                    appOpenAd?.show(it)
                } catch (ex: Exception) {
                    Log.d("", ex.message.toString())
                }
            }
        } else {
            LoadingUtils.dismissScreen()
        }
    }

    private var isColdStart: Boolean = true

    private fun onAppForegrounded() {
        if (isColdStart) {
            isColdStart = false
            return
        }
        if (isOpenAdStop || !CodecxAd.isSDKInit) {
            isOpenAdStop = false
            return
        }

        currentActivity?.let {
            val adConfig = RemoteConfigHelper.adIds.resumeAppOpen

            if (adConfig.isAdEnable && OpenAdConfig.isAdEnable() && CodecxAd.getAdConfig()?.isGoogleAdsAllowed == true && UMPConsent.isUMPAllowed && !isOpenAdStop) {
                fetchAd()
            }
        }

    }


    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }


    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        currentActivity = p0
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStopped(p0: Activity) {

    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}