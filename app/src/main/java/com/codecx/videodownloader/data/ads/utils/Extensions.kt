package com.codecx.videodownloader.data.ads.utils

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    val hasNetwork = capabilities != null
    val hasInternetCapability =
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val hasValidatedInternet =
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

    val hasInternet = hasNetwork && hasInternetCapability && hasValidatedInternet

    return hasInternet
}
fun runOnUiThread(work:()-> Unit){
    Handler(Looper.getMainLooper()).post {
        work()
    }
}
fun Dialog.safeDismiss() {
    if (isShowing) {
        dismiss()
    }
}

fun String.showLog(message: String) {
    Log.d(this, message)
}