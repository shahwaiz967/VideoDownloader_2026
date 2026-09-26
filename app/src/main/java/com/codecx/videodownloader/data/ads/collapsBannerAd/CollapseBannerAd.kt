package com.codecx.videodownloader.data.ads.collapsBannerAd

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.codecx.videodownloader.data.ads.utils.removeSelf
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.GDPR.UMPConsent
import com.codecx.videodownloader.data.ads.interfaces.AdCallBack
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig
import com.codecx.videodownloader.data.ads.utils.runOnUiThread
import com.codecx.videodownloader.data.utils.beVisible
import com.google.firebase.analytics.FirebaseAnalytics


object CollapseBannerAd {
    private val cacheAds = hashMapOf<String, AdView>()
    fun clearCollapseBannerCaches() {
        cacheAds.clear()
    }

    fun loadCollapseBanner(
        activity: Activity,
        isAdAllowed: Boolean,
        adContainer: ViewGroup?,
        unitId: String,
        position: String, adCallBack: AdCallBack
    ) {
        if (isAdAllowed && UMPConsent.isUMPAllowed && CodecxAd.isSDKInit) {
            val cacheAd = cacheAds[unitId]
            if (cacheAd != null) {
                adContainer?.removeAllViews()
                adContainer?.beVisible()
                cacheAd.removeSelf()
                adContainer?.addView(
                    cacheAd
                )
            } else {
                val extras = Bundle()
                extras.putString("collapsible", position)
                val adSize = getAdSize(activity)
                val adRequest = BannerAdRequest.Builder(unitId, adSize)
                    .setGoogleExtrasBundle(extras)
                    .build()
                val adView = AdView(activity)
                adView.loadAd(
                    adRequest = adRequest,
                    adLoadCallback = object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {
                            cacheAds[unitId] = adView
                            runOnUiThread {
                                adContainer?.removeAllViews()
                                adCallBack.onAdLoaded()
                                adContainer?.addView(adView)

                                ad.adEventCallback = object : BannerAdEventCallback {
                                    override fun onAdImpression() {
                                        super.onAdImpression()
                                        FirebaseAnalytics.getInstance(activity).logEvent(
                                            FirebaseAnalytics.Event.AD_IMPRESSION, Bundle().apply {
                                                putString(FirebaseAnalytics.Param.AD_FORMAT, "collapsible_banner")
                                                putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                                                putString(FirebaseAnalytics.Param.AD_UNIT_NAME, unitId)
                                            }
                                        )
                                        adCallBack.onAdShown()
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        adCallBack.onAdDismiss()

                                    }


                                    override fun onAdClicked() {
                                        if (CodecxAd.getAdConfig()?.isDisableResumeAdOnClick == true) {
                                            OpenAdConfig.isOpenAdStop = true
                                        }
                                    }
                                }

                            }
                        }

                        override fun onAdFailedToLoad(adError: com.google.android.libraries.ads.mobile.sdk.common.LoadAdError) {
                            runOnUiThread {
                                adCallBack.onAdFailToLoad(Exception(adError.message))
                                adContainer?.removeAllViews()
                            }
                        }
                    }
                )
            }


        } else {
            adContainer?.removeAllViews()
        }


    }


    private fun getAdSize(context: Context): AdSize {
        val outMetrics = context.resources.displayMetrics
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            adWidth
        )
    }

}