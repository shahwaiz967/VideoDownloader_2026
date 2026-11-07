package com.codecx.videodownloader.data.ads.GDPR

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object UMPConsent {
    var isUMPAllowed: Boolean = true
    fun requestForUMP(
        activity: Activity,
        enableTest: Boolean = false,
        deviceHashId: String = "B3EEABB8EE11C2BE770B684D95219ECB", listener: UMPRequestListener
    ) {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(deviceHashId)
            .build()

        val params =
            if (enableTest) {
                ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings)
                    .setTagForUnderAgeOfConsent(false)
                    .build()
            } else {
                ConsentRequestParameters.Builder()
                    .setTagForUnderAgeOfConsent(false)
                    .build()
            }

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) {
                    listener.onRequest(consentInformation.canRequestAds())
                }
            },
            {
                listener.onRequest(false)
            })
    }

    interface UMPRequestListener {
        fun onRequest(isAccepted: Boolean)
    }
}