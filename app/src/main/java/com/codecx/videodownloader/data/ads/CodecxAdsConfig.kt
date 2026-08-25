package com.codecx.videodownloader.data.ads


class CodecxAdsConfig {
    var adsTimeOut: Long = 10000
    var isDebugged = false
    var testDevices = listOf<String>()
    var isYandexAllowed = false
    var isGoogleAdsAllowed: Boolean = true
    var shouldShowYandexOnGoogleAdFail = false
    var isDisableResumeAdOnClick = true

    class Builder {
        private var testDevices: List<String> = listOf()
        private var isDebugged: Boolean = false
        private var isYandexAllowed: Boolean = false
        private var isDisableResumeAdOnClick = true
        private var adsTimeOut: Long = 10000
        private var isGoogleAdsAllowed: Boolean = true
        private var shouldShowYandexOnGoogleAdFail: Boolean = false
        private var showNext: Boolean = false
        fun  setAdsTimeOut(adsTimeOut: Long) = apply {
            this@Builder.adsTimeOut = adsTimeOut
        }
        fun setIsDebugged(isDebugged: Boolean) = apply {
            this@Builder.isDebugged = isDebugged
        }

        fun setDisableResumeAdOnClick(isDisableResumeAdOnClick: Boolean) = apply {
            this@Builder.isDisableResumeAdOnClick = isDisableResumeAdOnClick
        }

        fun setIsYandexAllowed(isYandexAllowed: Boolean) = apply {
            this@Builder.isYandexAllowed = isYandexAllowed
        }


        fun setIsGoogleAdsAllowed(isGoogleAdsAllowed: Boolean) = apply {
            this@Builder.isGoogleAdsAllowed = isGoogleAdsAllowed
        }

        fun setShowYandexOnGoogleAdFail(isShowYandexOnGoogleAdFail: Boolean) = apply {
            this@Builder.shouldShowYandexOnGoogleAdFail = isShowYandexOnGoogleAdFail
        }

        fun onNextInterstitial(value: Boolean) = apply {
            this@Builder.showNext = value
        }

        fun addTestDevices(testDevices: List<String>) =
            apply { this@Builder.testDevices = testDevices }

        fun build(): CodecxAdsConfig {
            val codecx = CodecxAdsConfig()
            codecx.apply {
                this.adsTimeOut = this@Builder.adsTimeOut
                this.isGoogleAdsAllowed = this@Builder.isGoogleAdsAllowed
                this.isDebugged = this@Builder.isDebugged
                this.isYandexAllowed = this@Builder.isYandexAllowed
                this.shouldShowYandexOnGoogleAdFail = this@Builder.shouldShowYandexOnGoogleAdFail
                this.testDevices = this@Builder.testDevices
                this.isDisableResumeAdOnClick = this@Builder.isDisableResumeAdOnClick
            }
            return codecx
        }
    }
}