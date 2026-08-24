package com.codecx.statussaver.core.utils.exceptions

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult

class PermissionRequired : Exception("App Need Permission to work properly") {

    fun requestForPermission(
        activity: Activity,
        activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        permissionResultLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissions.any {
                activity.shouldShowRequestPermissionRationale(it)
            }) {
            activityResultLauncher.launch(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", activity.packageName, null)
            })
        } else {
            permissionResultLauncher.launch(permissions)
        }

    }
}