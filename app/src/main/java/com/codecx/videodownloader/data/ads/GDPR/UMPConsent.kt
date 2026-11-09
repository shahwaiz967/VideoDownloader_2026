package com.codecx.videodownloader.data.ads.GDPR

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object UMPConsent {
    var isUMPAllowed: Boolean = true

    fun requestForUMP(
        activity: Activity,
        enableTest: Boolean = false,
        deviceHashId: String = "B3EEABB8EE11C2BE770B684D95219ECB",
        listener: UMPRequestListener
    ) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        if (enableTest) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(deviceHashId)
                .build()
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            paramsBuilder.build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    listener.onRequest(isUMPAllowed)
                }
            },
            { _ ->
                listener.onRequest(isUMPAllowed)
            }
        )
    }

    fun showPrivacyOptionsForm(activity: Activity, onComplete: ((Boolean) -> Unit)? = null) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        if (consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ ->
                isUMPAllowed = consentInformation.canRequestAds()
                onComplete?.invoke(isUMPAllowed)
            }
        } else {
            onComplete?.invoke(consentInformation.canRequestAds())
        }
    }

    interface UMPRequestListener {
        fun onRequest(isAccepted: Boolean)
    }
}