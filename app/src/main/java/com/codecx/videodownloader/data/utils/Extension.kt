package com.codecx.videodownloader.data.utils

import android.R
import android.app.Activity
import android.app.role.RoleManager
import android.content.ClipboardManager
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.codecx.videodownloader.data.states.ResultState
import com.codecx.videodownloader.MainActivity
import com.codecx.videodownloader.features.ui.fragments.DownloadOptionBottomSheet
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
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

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
        NetworkCapabilities.NET_CAPABILITY_VALIDATED
    )
}

fun Context.getClipboardText(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text?.toString()
}

fun Activity.removeBlur() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val v = window.decorView.findViewById<View?>(R.id.content)
        v?.setRenderEffect(null)
    }
}

fun Fragment.observeDownloadState(
    viewModel: DownloadViewModel,
    onLoading: () -> Unit,
    onSuccess: () -> Unit,
    onReset: () -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.downloadInfo.collectLatest { result ->
                when (result) {
                    is ResultState.Success -> {
                        onSuccess()
                        DownloadOptionBottomSheet.newInstance()
                            .show(parentFragmentManager, DownloadOptionBottomSheet.TAG)
                    }

                    is ResultState.Loading -> {
                        onLoading()
                    }

                    is ResultState.Error -> {
                        requireContext().showToast(result.message)
                        viewModel.clearResult()
                        onReset()
                    }

                    is ResultState.Init -> {
                        onReset()
                    }
                }
            }
        }
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
fun String.isValidUrl(): Boolean {
    return Patterns.WEB_URL.matcher(this).matches() ||
            this.startsWith("http://") ||
            this.startsWith("https://") ||
            this.startsWith("www.")
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

fun View.safeNavigate(id: Int, args: Bundle? = null, navOptions: NavOptions? = null) {
    try {
        findNavController().navigate(id, args, navOptions)
    } catch (e: Exception) {
        Log.e("safeNavigate", "Navigation failed: ${e.message}")
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


val Fragment.currentActivity get() = if (isAdded) activity as? MainActivity else null

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

        //Toast.makeText(this, getString(R.string.cache_cleared_successfully), Toast.LENGTH_SHORT).show()
    }catch (e: Exception){
        e.printStackTrace()
      //  Toast.makeText(this, getString(R.string.failed_to_clear_cache), Toast.LENGTH_SHORT).show()
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

object ThemeManager {

    private const val PREFS = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDarkMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()

        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}


enum class SearchEngine(val title: String, val searchUrl: String, val homeUrl: String){
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com"),
    BING("Bing", "https://www.bing.com/search?q=", "https://www.bing.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
    BAIDU("Baidu", "https://www.baidu.com/s?wd=", "https://www.baidu.com"),
    YANDEX("Yandex", "https://yandex.com/search/?text=", "https://yandex.com"),
    YAHOO("Yahoo", "https://search.yahoo.com/search?p=", "https://search.yahoo.com"),
    SOGOU("Sogou", "https://www.sogou.com/web?query=", "https://www.sogou.com"),
    QIK("Qik", "https://www.qik.com/search?q=", "https://www.qik.com"),
    BRAVE("Brave", "https://search.brave.com/search?q=", "https://search.brave.com"),
    STARTPAGE("Startpage", "https://startpage.com/sp/search?query=", "https://startpage.com"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=", "https://www.ecosia.org"),
}




object SearchEngineManager {
    private const val PREFS = "search_engine_prefs"
    private const val KEY_SEARCH_ENGINE = "search_engine"

    fun getSelectedSearchEngine(context: Context): SearchEngine {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SEARCH_ENGINE, SearchEngine.GOOGLE.name)
        return try {
            SearchEngine.valueOf(name ?: SearchEngine.GOOGLE.name)
        } catch (e: Exception) {
            SearchEngine.GOOGLE
        }
    }

    fun setSelectedSearchEngine(context: Context, searchEngine: SearchEngine) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SEARCH_ENGINE, searchEngine.name)
            .apply()
    }
}

fun String.printLog(message: String) {
    Log.d(this, message)
}


object PrefManager {
    private const val PREFS = "app_prefs"
    private const val KEY_FIRST_RUN = "is_first_run"

    fun isFirstRun(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_FIRST_RUN, true)
    }

    fun setFirstRun(context: Context, isFirstRun: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FIRST_RUN, isFirstRun)
            .apply()
    }
}



