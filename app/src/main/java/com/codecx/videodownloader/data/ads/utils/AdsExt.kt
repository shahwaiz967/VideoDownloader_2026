package com.codecx.videodownloader.data.ads.utils

import android.graphics.Color
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.CodecxAd
import com.codecx.videodownloader.data.ads.bannerAds.BannerAd
import com.codecx.videodownloader.data.ads.collapsBannerAd.CollapseBannerAd
import com.codecx.videodownloader.data.ads.enums.EnumAdType
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper.TimeDelayException
import com.codecx.videodownloader.data.ads.interfaces.AdCallBack
import com.codecx.videodownloader.data.ads.nativeAds.NativeAdsUtil
import com.codecx.videodownloader.data.dataClasses.AdItem
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper.BANNER_AD_TYPE
import com.codecx.videodownloader.data.utils.RemoteConfigHelper.COLLAPSABLE_AD_TYPE
import com.codecx.videodownloader.data.utils.currentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class ErrorType {
    InDelay,
    Other;
}

fun View.removeSelf() {
    if (parent != null) {
        (parent as? ViewGroup)?.removeView(this)
    }
}

@ColorInt
fun Int.getContrastingTextColor(): Int {
    val luminance = ColorUtils.calculateLuminance(this)
    return if (luminance < 0.5) Color.WHITE else Color.BLACK
}

fun Fragment.showInter(
    interstitialHelper: InterstitialAdHelper.InterstitialHelper,
    @StringRes stringResId: Int,
    adAllowed: Boolean,
    enableTimer: Boolean = true,
    onDismiss: (ErrorType) -> Unit
) {
    if (currentActivity != null) {
        interstitialHelper.loadShowInterstitial(
            adId = getString(stringResId),
            adAllowed = adAllowed,
            timerAllowed = enableTimer,
            showLoadingLayout = true,
            timerMilliSec =10000L,
            activity = currentActivity!!,
            adCallBack = object : AdCallBack() {
                override fun onAdDismiss() {
                    super.onAdDismiss()
                    onDismiss(ErrorType.Other)
                }

                override fun onAdFailToShow(error: Exception) {
                    super.onAdFailToShow(error)
                    onDismiss(ErrorType.Other)
                }

                override fun onAdFailToLoad(error: Exception) {
                    super.onAdFailToLoad(error)
                    if (error is TimeDelayException) {
                        onDismiss(ErrorType.InDelay)
                    } else {
                        onDismiss(ErrorType.Other)
                    }
                }
            })
    } else {
        onDismiss(ErrorType.Other)
    }
}

fun Fragment.showInter(
    interstitialHelper: InterstitialAdHelper.InterstitialHelper,
    stringId: String,
    adAllowed: Boolean,
    onDismiss: () -> Unit,
    onFail: () -> Unit
) {
    if (currentActivity != null && CodecxAd.isSDKInit) {
        interstitialHelper.loadShowInterstitial(
            adId = stringId,
            adAllowed = adAllowed,
            showLoadingLayout = true,
            activity = currentActivity!!,
            adCallBack = object : AdCallBack() {
                override fun onAdDismiss() {
                    super.onAdDismiss()
                    interstitialHelper.lastAdTimer = SystemClock.elapsedRealtime()
                    onDismiss()
                }

                override fun onAdFailToShow(error: Exception) {
                    super.onAdFailToShow(error)
                    onFail()
                }

                override fun onAdFailToLoad(error: Exception) {
                    super.onAdFailToLoad(error)
                    onFail()

                }
            })
    } else {
        onDismiss()
    }
}

fun Fragment.bindNative(
    stringResId: String,
    adAllowed: Boolean,
    adType: EnumAdType,
    container: ViewGroup?,
    ctrColor: String,
    adTagColor: String
) {
    view?.doOnPreDraw {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            if (currentActivity != null) {
//                val shimmerLayout = when (adType) {
//                    EnumAdType.BannerNative -> {
//                        R.layout.native_banner_shimmer
//                    }
//
//                    EnumAdType.MediumNative -> {
//                        R.layout.medium_shimmer
//
//                    }
//
//                    EnumAdType.MediumMediaNative -> {
//                        R.layout.medium_media_shimmer
//
//                    }
//
//                    else -> {
//                        R.layout.large_native_shimmer
//                    }
//                }
//                val contentLayout = when (adType) {
//                    EnumAdType.BannerNative -> {
//                        R.layout.native_banner
//                    }
//
//                    EnumAdType.MediumNative -> {
//                        R.layout.medium_native
//
//                    }
//
//                    EnumAdType.MediumMediaNative -> {
//                        R.layout.medium_media_native
//
//                    }
//
//                    else -> {
//                        R.layout.large_native
//                    }
//                }
//                activity?.let {
//                    NativeAdsUtil.populateNativeAd(
//                        isAdAllowed = adAllowed,
//                        loadingAdView = if (requireContext().isRussia()) shimmerLayout.second else shimmerLayout.first,
//                        nativeAdView = if (requireContext().isRussia()) contentLayout.second else contentLayout.first,
//                        adContainerView = container,
//                        nativeId = stringResId,
//                        context = it,
//                        adListener = null
//                    )
//                }
            }
        }
    }
}


fun Fragment.bindNative(adItem: AdItem, isPro: Boolean, container: ViewGroup?) {
    if (currentActivity != null) {
        when (adItem.adFormat) {
            BANNER_AD_TYPE -> {
                BannerAd.showBanner(adItem, isPro, container, requireActivity(), null)
            }

            COLLAPSABLE_AD_TYPE -> {
                CollapseBannerAd.loadCollapseBanner(
                    requireActivity(),
                    adItem.isAdEnable && !isPro,
                    container,
                    adItem.adId,
                    object : AdCallBack() {

                    }
                )
            }

            else -> {
                bindNative(
                    stringResId = adItem.adId,
                    adAllowed = adItem.isAdEnable && !isPro,
                    adType = adItem.toAdSize(),
                    container = container,
                    ctrColor = adItem.ctrColor,
                    adTagColor = adItem.adTagColor
                )
            }
        }
    }
}