package com.codecx.videodownloader.data.utils

import android.R
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.codecx.videodownloader.features.ui.activities.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


fun Activity.applyBlur() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val v = window.decorView.findViewById<View?>(R.id.content)
        val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
        v?.setRenderEffect(blurEffect)
    }
}

fun Activity.removeBlur() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val v = window.decorView.findViewById<View?>(R.id.content)
        v?.setRenderEffect(null)
    }
}


fun Int.toDp(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

fun ImageView.animateMockup() {
    val animation = AnimationSet(true).apply {
        addAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        interpolator = LinearInterpolator()
    }
    startAnimation(animation)
}

fun ImageView.loadGif(id: Int) {
    Glide.with(this)
        .load(id)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun Fragment.safePopBackStack() {
    if (this.isAdded && this.view != null && this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun View.handleSafeClick(listener: View.OnClickListener) {
    setOnClickListener {
        it.isEnabled = false
        postDelayed(object : Runnable {
            override fun run() {
                it.isEnabled = true
                removeCallbacks(this)
            }

        }, 200)
        listener.onClick(it)
    }
}


fun ImageView.setImage(imageId: Any?) {
    Glide.with(this).load(imageId).into(this)
}

fun ImageView.setImage(imageId: Any?, placeHolder: Any?) {
    Glide.with(this).load(imageId).error(placeHolder).into(this)
}

fun Context.showToast(message: String?) {
    message?.let {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
}


fun View.beGone() {
    if (this.isVisible || this.isInvisible)
        this.visibility = View.GONE
}

fun View.beInvisible() {
    if (this.isVisible || this.isGone)
        this.visibility = View.INVISIBLE
}

fun View.beVisible() {
    if (this.isGone || this.isInvisible)
        this.visibility = View.VISIBLE
}

fun View.beVisibleIf(value: Boolean) {
    if (value) {
        beVisible()
    } else {
        beGone()
    }

}

fun View.beInvisibleIf(value: Boolean) {
    if (value) {
        beInvisible()
    } else {
        beVisible()
    }
}

fun String.showLog(message: String?) {
    message?.let {
        Log.d(this, it)
    }
}

fun Long.getDateFormat(pattern: String = "MMM,dd yyyy"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this).toString()
}

fun Long.getTimeFormat(pattern: String = "hh:mm a"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this).toString()
}

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

fun Long.formatFileSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        Locale.getDefault(),
        "%.1f %s",
        this / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun Activity.keepScreenActive() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun Fragment.safeNavigate(id: Int, args: Bundle? = null, navOptions: NavOptions? = null) {
    try {
        runWhenResumed {
            findNavController().navigate(id, args, navOptions)
        }
    } catch (e: IllegalArgumentException) {
        Log.e("safeNavigate", "Navigation failed: destination not found. ${e.message}")
    } catch (e: Exception) {
        Log.e("safeNavigate", "Navigation failed with exception: ${e.message}")
    }
}


inline fun <reified T> T.toJson(): String {
    return Gson().toJson(this)
}

inline fun <reified T> String.fromJson(): T? {
    return try {
        Gson().fromJson(this, T::class.java)
    } catch (ex: Exception) {
        Log.e("fromJson", "fromJson: ${ex.message}")
        null
    }
}


val Fragment.currentActivity get() = if (isVisible || isAdded) activity as? MainActivity else null

fun Fragment.handleBackPress(enable: Boolean = true, onPress: () -> Unit) {
    currentActivity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object :
        OnBackPressedCallback(enable) {
        override fun handleOnBackPressed() {
            onPress()
        }
    })
}


fun Fragment.attachWithLifeCycle(state: Lifecycle.State = Lifecycle.State.STARTED, work: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(state, work)
    }
}


fun Fragment.runWhenResumed(action: () -> Unit) {
    if (!isAdded || view == null) return
    if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        action()
    } else {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewLifecycleOwner.lifecycle.withResumed {
                    action()
                }
            } catch (e: Exception) {

            }
        }
    }
}

fun Context.clearAppCache(){
    try {
        cacheDir?.deleteRecursively()
        externalCacheDir?.deleteRecursively()

        Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
    }catch (e: Exception){
        e.printStackTrace()
        Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show()
    }
}

private fun File.deleteRecursively(): Boolean {
    if (isDirectory) {
        val children = listFiles() ?: return false
        for (child in children) {
            child.deleteRecursively()
        }
    }
    return delete()
}

fun Context.isDefaultBrowser(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(RoleManager::class.java)
        roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)
    } else {
        false
    }
}

