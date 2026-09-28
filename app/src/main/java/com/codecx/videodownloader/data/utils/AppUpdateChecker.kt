package com.codecx.videodownloader.data.utils

import android.app.Activity.RESULT_OK
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateChecker : DefaultLifecycleObserver {

    private var currentUpdateType = AppUpdateType.FLEXIBLE
    private var onUpdateComplete: (() -> Unit)? = null

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    private var activity: AppCompatActivity? = null
    private lateinit var appUpdateManager: AppUpdateManager
    fun registerActivity(activity: AppCompatActivity) {
        this.activity = activity
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        updateResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode != RESULT_OK) {
                if (currentUpdateType == AppUpdateType.IMMEDIATE) {
                    checkAppUpdate(onUpdateComplete ?: {})
                } else {
                    onUpdateComplete?.invoke()
                }
            } else {
                onUpdateComplete?.invoke()
            }
        }
        activity.lifecycle.addObserver(this)
    }

    private var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null


    fun checkAppUpdate(onComplete: () -> Unit) {
        this.onUpdateComplete = onComplete
        appUpdateManager.registerListener(installStateUpdatedListener)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable =
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            currentUpdateType = if (appUpdateInfo.updatePriority() >= 4) {
                AppUpdateType.IMMEDIATE
            } else {
                AppUpdateType.FLEXIBLE
            }

            if (isUpdateAvailable && appUpdateInfo.isUpdateTypeAllowed(currentUpdateType) && updateResultLauncher != null) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateResultLauncher!!,
                    AppUpdateOptions.newBuilder(currentUpdateType).build()
                )
            } else {
                onComplete()
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS && updateResultLauncher != null) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateResultLauncher!!,
                    AppUpdateOptions.newBuilder(currentUpdateType).build()
                )
            }
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity?.lifecycle?.removeObserver(this)
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }



    fun popupSnackbarForCompleteUpdate() {
        activity?.findViewById<View>(android.R.id.content)?.let { container ->
            Snackbar.make(
                container,
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("RESTART") { appUpdateManager.completeUpdate() }
                show()
            }
        }

    }
}