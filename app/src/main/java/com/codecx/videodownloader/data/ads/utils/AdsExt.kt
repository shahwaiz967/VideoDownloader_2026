package com.codecx.videodownloader.data.ads.utils

import android.app.Dialog
import android.graphics.Color
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.codecx.videodownloader.data.ads.enums.getNativeLayout
import com.codecx.videodownloader.data.ads.enums.getShimmerLayout
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper.TimeDelayException
import com.codecx.videodownloader.data.ads.interfaces.AdCallBack
import com.codecx.videodownloader.data.ads.nativeAds.NativeAdsUtil
import com.codecx.videodownloader.data.models.AdItem
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper.BANNER_AD_TYPE
import com.codecx.videodownloader.data.utils.RemoteConfigHelper.COLLAPSIBLE_AD_TYPE
import com.codecx.videodownloader.data.utils.currentActivity
import com.codecx.videodownloader.MainActivity
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ErrorType {
    InDelay,
    Other;
}

object AdCounter {
    private var clickCount = 0

    fun incrementAndCheck(frequency: Int): Boolean {
        clickCount++
        if (clickCount >= frequency) {
            return true
        }
        return false
    }

    fun reset() {
        clickCount = 0
    }
}

fun Fragment.showInterWithCounter(
    interstitialHelper: InterstitialAdHelper.InterstitialHelper,
    adItem: AdItem,
    onComplete: () -> Unit
) {
    val frequency = RemoteConfigHelper.adIds.adFrequency
    if (AdCounter.incrementAndCheck(frequency)) {
        showInter(
            interstitialHelper = interstitialHelper,
            stringId = adItem.adId,
            adAllowed = adItem.isAdEnable,
            onDismiss = {
                AdCounter.reset()
                onComplete()
            },
            onFail = {
                showFullScreenNativeFallback(adItem, onComplete)
            }
        )
    } else {
        onComplete()
    }
}

fun Fragment.showFullScreenNativeFallback(adItem: AdItem, onComplete: () -> Unit) {
    val activity = activity ?: run {
        onComplete()
        return
    }

    val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val view = LayoutInflater.from(activity).inflate(R.layout.large_native, null)
    dialog.setContentView(view)
    dialog.setOnDismissListener {
        onComplete()
    }

    val adContainer = view as? NativeAdView

    NativeAdsUtil.loadNativeAd(
        isAdAllowed = adItem.isAdEnable,
        loadingAdView = R.layout.large_native_shimmer,
        adContainerView = null,
        nativeId = adItem.adId,
        context = activity,
        adListener = object : AdCallBack() {
            override fun onNativeAdLoad(nativeAd: NativeAd) {
                if (adContainer != null) {
                    NativeAdsUtil.populateNativeAd(
                        context = activity,
                        adContainerView = view as ViewGroup,
                        nativeAd = nativeAd,
                        nativeAdView = R.layout.large_native,
                        adListener = object : AdCallBack() {
                            override fun onAdShown() {
                                dialog.show()
                                AdCounter.reset()
                            }
                        }
                    )
                } else {
                    onComplete()
                }
            }

            override fun onAdFailToLoad(error: Exception) {
                onComplete()
            }
        }
    )
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
    val mainActivity = activity as? MainActivity
    if (mainActivity != null) {
        interstitialHelper.loadShowInterstitial(
            adId = getString(stringResId),
            adAllowed = adAllowed,
            timerAllowed = enableTimer,
            showLoadingLayout = true,
            timerMilliSec = 10000L,
            activity = mainActivity,
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
    val mainActivity = activity as? MainActivity
    if (mainActivity != null && CodecxAd.isSDKInit) {
        interstitialHelper.loadShowInterstitial(
            adId = stringId,
            adAllowed = adAllowed,
            showLoadingLayout = true,
            activity = mainActivity,
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
    shimmerLayout: Int,
    contentLayout: Int,
    container: ViewGroup?
) {
    view?.doOnPreDraw {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            if (currentActivity != null) {
                activity?.let {
                    NativeAdsUtil.populateNativeAd(
                        isAdAllowed = adAllowed,
                        loadingAdView = shimmerLayout,
                        nativeAdView = contentLayout,
                        adContainerView = container,
                        nativeId = stringResId,
                        context = it,
                        adListener = null
                    )
                }
            }
        }
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
    val shimmerLayout = when (adType) {
        EnumAdType.BannerNative -> R.layout.native_banner_shimmer
        EnumAdType.MediumNative -> R.layout.medium_shimmer
        EnumAdType.MediumMediaNative -> R.layout.medium_media_shimmer
        else -> R.layout.large_native_shimmer
    }
    val contentLayout = when (adType) {
        EnumAdType.BannerNative -> R.layout.native_banner
        EnumAdType.MediumNative -> R.layout.medium_native
        EnumAdType.MediumMediaNative -> R.layout.medium_media_native
        else -> R.layout.large_native
    }

    bindNative(
        stringResId = stringResId,
        adAllowed = adAllowed,
        shimmerLayout = shimmerLayout,
        contentLayout = contentLayout,
        container = container
    )
}

fun Fragment.bindNative(adItem: AdItem, isPro: Boolean, container: ViewGroup?) {
    if (currentActivity != null) {
        if (!adItem.isAdEnable || isPro || adItem.adType == 0) {
            container?.removeAllViews()
            container?.visibility = View.GONE
            return
        }
        container?.visibility = View.VISIBLE
        when (adItem.adFormat) {
            BANNER_AD_TYPE -> {
                val bannerType = if (adItem.adSize.equals("large", ignoreCase = true)) 2 else 0
                BannerAd.showBanner(adItem, isPro, container, requireActivity(), null, bannerType)
            }

            COLLAPSIBLE_AD_TYPE -> {
                CollapseBannerAd.loadCollapseBanner(
                    requireActivity(),
                    adItem.isAdEnable && !isPro,
                    container,
                    adItem.adId,
                    object : AdCallBack() {}
                )
            }

            else -> {
                bindNative(
                    stringResId = adItem.adId,
                    adAllowed = adItem.isAdEnable && !isPro,
                    shimmerLayout = adItem.getShimmerLayout(),
                    contentLayout = adItem.getNativeLayout(),
                    container = container
                )
            }
        }
    }
}
