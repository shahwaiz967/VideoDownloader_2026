package com.codecx.videodownloader.data.ads.bannerAds

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.utils.removeSelf
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.GDPR.UMPConsent
import com.codecx.videodownloader.data.ads.interfaces.AdCallBack
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig
import com.codecx.videodownloader.data.ads.utils.runOnUiThread
import com.codecx.videodownloader.data.models.AdItem
import com.codecx.videodownloader.data.utils.beVisible
import com.codecx.videodownloader.data.utils.toDp
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.firebase.analytics.FirebaseAnalytics

object BannerAd {
    private val cacheAds = hashMapOf<String, AdView>()

    fun showBanner(
        adItem: AdItem,
        isPro: Boolean,
        adContainer: ViewGroup?,
        context: Context,
        adCallBack: AdCallBack?,
        type: Int = 0
    ) {
        if (adItem.isAdEnable && !isPro && UMPConsent.isUMPAllowed && CodecxAd.getAdConfig()?.isGoogleAdsAllowed == true) {
            val cacheAd = cacheAds[adItem.adId]
            if (cacheAd != null) {
                adContainer?.removeAllViews()
                adContainer?.beVisible()
                cacheAd.removeSelf()
                adContainer?.addView(cacheAd)
            } else {
                val adSize = when (type) {
                    4 -> AdSize.MEDIUM_RECTANGLE
                    else ->   AdSize.BANNER
                }
                val adView = AdView(context)
                adContainer?.removeAllViews()
                adContainer?.beVisible()
                val loadingLayout = LayoutInflater.from(context)
                    .inflate(R.layout.banner_shimmer, adContainer, false)
                loadingLayout?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    this.height = adSize.height.toDp(context)
                }
                adContainer?.addView(
                    loadingLayout
                )
                val adListener = object : AdLoadCallback<BannerAd> {
                    override fun onAdLoaded(ad: BannerAd) {
                        super.onAdLoaded(ad)
                        cacheAds[adItem.adId] = adView
                        runOnUiThread {
                            ad.adEventCallback = object : BannerAdEventCallback {
                                override fun onAdClicked() {
                                    super.onAdClicked()
                                    if (CodecxAd.getAdConfig()?.isDisableResumeAdOnClick == true) {
                                        OpenAdConfig.isOpenAdStop = true
                                    }
                                }

                                override fun onAdImpression() {
                                    super.onAdImpression()
                                    FirebaseAnalytics.getInstance(context).logEvent(
                                        FirebaseAnalytics.Event.AD_IMPRESSION, Bundle().apply {
                                            putString(FirebaseAnalytics.Param.AD_FORMAT, "banner")
                                            putString(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                                            putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adItem.adId)
                                        }
                                    )
                                    adCallBack?.onAdShown()
                                }

                            }
                            adContainer?.removeAllViews()
                            adCallBack?.onAdLoaded()
                            adContainer?.addView(adView)
                        }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        runOnUiThread {
                            adCallBack?.onAdFailToLoad(Exception(adError.message))
                            adContainer?.removeAllViews()
                        }
                    }
                }
                val adRequest = BannerAdRequest.Builder(
                    adItem.adId,
                    adSize
                ).build()

                adView.loadAd(adRequest, adListener)
            }

        } else {
            adContainer?.removeAllViews()
        }


    }

    fun clearBannerCaches() {
        cacheAds.clear()
    }

    private fun getAdSize(context: Context): AdSize {
//        val outMetrics = context.resources.displayMetrics
//        val widthPixels = outMetrics.widthPixels.toFloat()
//        val density = outMetrics.density
//        val adWidth = (widthPixels / density).toInt()
//        return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
//            context,
//            adWidth
//        )
        return AdSize.BANNER
    }


}