package com.codecx.videodownloader.data.ads.nativeAds

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.graphics.toColorInt
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.GDPR.UMPConsent
import com.codecx.videodownloader.data.ads.interfaces.AdCallBack
import com.codecx.videodownloader.data.ads.openAd.OpenAdConfig
import com.codecx.videodownloader.data.ads.utils.isNetworkConnected
import com.codecx.videodownloader.data.ads.utils.runOnUiThread
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView


object NativeAdsUtil {

    private val cacheAds = hashMapOf<String, NativeAd>()
    fun clearNativeCaches() {
        cacheAds.values.forEach { ad ->
            ad.destroy()
        }
        cacheAds.clear()
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.apply {
            headlineView = findViewById(R.id.ad_headline)
            bodyView = findViewById(R.id.ad_body)
           // starRatingView = findViewById(R.id.ad_stars)
            callToActionView = findViewById(R.id.ad_call_to_action_new)
            iconView = findViewById(R.id.ad_app_icon)
         //   advertiserView = findViewById(R.id.ad_advertiser)

            (headlineView as? TextView)?.text = nativeAd.headline
            (headlineView as? TextView)?.isSelected = true

            nativeAd.mediaContent.let {
                mediaView?.mediaContent = it
            }
        }
        nativeAd.starRating?.let { ratings ->
            adView.starRatingView?.visibility = View.VISIBLE
            if (adView.starRatingView != null) {
                (adView.starRatingView as RatingBar).rating = ratings.toFloat()
            } else {
                adView.starRatingView?.visibility = View.GONE
            }
        } ?: run {
            adView.starRatingView?.visibility = View.GONE
        }

        nativeAd.body?.let { body ->
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as? TextView)?.apply {
                text = body
                post { isSelected = true }
            }
        } ?: run {
            adView.bodyView?.visibility = View.INVISIBLE
        }

        nativeAd.advertiser?.let { advertiser ->
            adView.advertiserView?.visibility = View.VISIBLE
            if (adView.advertiserView != null) {
                (adView.advertiserView as TextView).text = advertiser
            }
        } ?: run {
            adView.advertiserView?.visibility = View.GONE
        }

        nativeAd.callToAction?.let { callToAction ->
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = callToAction
        } ?: run {
            adView.callToActionView?.visibility = View.INVISIBLE
        }

        nativeAd.icon?.let {
            adView.iconView?.visibility = View.VISIBLE
            adView.iconView?.let { iconView ->
                (iconView as ImageView).setImageDrawable(it.drawable)
            }
        } ?: run {
            adView.iconView?.visibility = View.INVISIBLE
        }

        adView.registerNativeAd(nativeAd, adView.findViewById(R.id.ad_media))
    }

    fun loadNativeAd(
        isAdAllowed: Boolean,
        @LayoutRes loadingAdView: Int,
        adContainerView: ViewGroup?,
        nativeId: String, context: Activity, adListener: AdCallBack? = null
    ) {
        if (isAdAllowed && UMPConsent.isUMPAllowed && WebView.getCurrentWebViewPackage() != null) {
            if (context.isNetworkConnected()) {
                val loadingView = LayoutInflater.from(adContainerView?.context ?: context)
                    .inflate(loadingAdView, adContainerView, false)
                adContainerView?.removeAllViews()
                adContainerView?.addView(loadingView)
                val adRequest =
                    NativeAdRequest.Builder(nativeId, listOf(NativeAd.NativeAdType.NATIVE))
                        .build()
                NativeAdLoader.load(
                    adRequest = adRequest,
                    adLoadCallback = object : NativeAdLoaderCallback {
                        override fun onNativeAdLoaded(nativeAd: NativeAd) {
                            runOnUiThread {
                                nativeAd.adEventCallback = object : NativeAdEventCallback {
                                    override fun onAdClicked() {
                                        adListener?.onAdClick()
                                    }

                                }
                                adListener?.onNativeAdLoad(nativeAd)
                            }
                        }


                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            runOnUiThread {
                                adListener?.onAdFailToLoad(Exception(adError.message))
                            }
                        }
                    }
                )
            } else {
                adContainerView?.removeAllViews()
                adListener?.onAdFailToShow(Exception("Internet not connected"))
            }
        } else {
            adContainerView?.removeAllViews()
            adListener?.onAdFailToShow(Exception("Ad not allowed"))
        }
    }


    private fun populateNativeAd(
        context: Activity,
        adContainerView: ViewGroup,
        nativeAd: NativeAd, @LayoutRes nativeAdView: Int, adListener: AdCallBack?
    ) {
        adContainerView.removeAllViews()
        val nativeView = LayoutInflater.from(context).inflate(nativeAdView, null, false)

        if (nativeView is NativeAdView) {
            adListener?.onAdShown()
            adContainerView.addView(nativeView)
            populateNativeAdView(nativeAd, nativeView)
        } else {
            adListener?.onAdFailToShow(Exception("Type not match"))
        }

    }

    fun populateNativeAd(
        isAdAllowed: Boolean,
        @LayoutRes loadingAdView: Int,
        @LayoutRes nativeAdView: Int,
        adContainerView: ViewGroup?,
        nativeId: String,
        context: Activity,
        adListener: AdCallBack? = null,
        containerColor: Int = "#D5D2D2".toColorInt(),
    ) {
        if (WebView.getCurrentWebViewPackage() != null && CodecxAd.isSDKInit) {
            if (CodecxAd.getAdConfig()?.isGoogleAdsAllowed == true) {
                val cacheAd = cacheAds[nativeId]
                if (cacheAd != null) {
                    if (adContainerView != null) {
                        populateNativeAd(
                            context = context,
                            adContainerView = adContainerView,
                            nativeAd = cacheAd,
                            nativeAdView = nativeAdView,
                            adListener = adListener

                        )
                    }
                } else {
                    loadNativeAd(
                        isAdAllowed = isAdAllowed,
                        loadingAdView = loadingAdView,
                        adContainerView = adContainerView,
                        nativeId = nativeId,
                        context = context,
                        adListener = object : AdCallBack() {
                            override fun onAdClick() {
                                super.onAdClick()
                                if (CodecxAd.getAdConfig()?.isDisableResumeAdOnClick == true) {
                                    OpenAdConfig.isOpenAdStop = true
                                }
                            }

                            override fun onNativeAdLoad(nativeAd: NativeAd) {
                                super.onNativeAdLoad(nativeAd)
                                cacheAds[nativeId] = nativeAd
                                adListener?.onNativeAdLoad(nativeAd)
                                if (adContainerView != null) {
                                    populateNativeAd(
                                        context = context,
                                        adContainerView = adContainerView,
                                        nativeAd = nativeAd,
                                        nativeAdView = nativeAdView,
                                        adListener = adListener

                                    )
                                }

                            }

                            override fun onAdFailToLoad(error: Exception) {
                                super.onAdFailToLoad(error)
                                adContainerView?.removeAllViews()
                                adListener?.onAdFailToLoad(error)
                            }

                            override fun onAdFailToShow(error: Exception) {
                                super.onAdFailToShow(error)
                                adListener?.onAdFailToShow(error)
                            }
                        }
                    )
                }
            }
        }


    }




}








