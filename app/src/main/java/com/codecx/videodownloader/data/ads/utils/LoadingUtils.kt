package com.codecx.videodownloader.data.ads.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.codecx.videodownloader.R

object LoadingUtils {
    private var dialog: Dialog? = null
    fun showAdLoadingScreen(context: Context, layoutId: Int) {
        try {
            if (dialog?.isShowing != true) {
                val adLoadingView =
                    LayoutInflater.from(context).inflate(layoutId, null, false)
                dialog = Dialog(context, R.style.FullScreenDialog)
                adLoadingView?.let {
                    dialog?.setContentView(it)
                    val layoutParams = WindowManager.LayoutParams()
                    layoutParams.copyFrom(dialog?.window?.attributes)
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                    dialog?.window?.attributes = layoutParams
                    dialog?.setCancelable(false)
                    dialog?.show()
                }
            }

        } catch (ex: Exception) {
            "asdjap".showLog(ex.message.toString())
        }
    }

    val isShowing: Boolean get() = dialog?.isShowing == true

    fun dismissScreen() {
        try {
            dialog?.safeDismiss()
        } catch (ex: java.lang.Exception) {
            "asdjap".showLog(ex.message.toString())
        }
    }
}