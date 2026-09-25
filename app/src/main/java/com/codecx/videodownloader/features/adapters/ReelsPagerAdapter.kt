package com.codecx.videodownloader.features.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.nativeAds.NativeAdsUtil
import com.codecx.videodownloader.data.models.AdItem
import com.codecx.videodownloader.data.models.ReelItem
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.databinding.ItemReelVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@androidx.media3.common.util.UnstableApi
class ReelsPagerAdapter(
    private val context: Context,
    initialItems: List<ReelItem>,
    private val onDownloadClick: (ReelItem.VideoItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_VIDEO = 0
        private const val VIEW_TYPE_AD = 1

        fun getAdControllerValue(adConfig: AdItem = RemoteConfigHelper.adIds.reelFullNative): Int {
            return when {
                RemoteConfigHelper.adIds.reelAdFrequency > 0 -> RemoteConfigHelper.adIds.reelAdFrequency
                else -> RemoteConfigHelper.adIds.adFrequency
            }
        }

        fun buildReelListWithAds(
            videoItems: List<ReelItem.VideoItem>,
            adConfig: AdItem = RemoteConfigHelper.adIds.reelFullNative,
            adController: Int = getAdControllerValue(adConfig)
        ): List<ReelItem> {
            val result = mutableListOf<ReelItem>()
            if (adConfig.isAdEnable && adController > 0) {
                var count = 0
                for (video in videoItems) {
                    result.add(video)
                    count++
                    if (count % adController == 0) {
                        result.add(ReelItem.NativeAdItem(adConfig))
                    }
                }
            } else {
                result.addAll(videoItems)
            }
            return result
        }
    }

    private var items: MutableList<ReelItem> = initialItems.toMutableList()
    private var rawVideoItems: List<ReelItem.VideoItem> = initialItems.filterIsInstance<ReelItem.VideoItem>()
    private var currentAdController: Int = getAdControllerValue()

    private val players = mutableMapOf<Int, ExoPlayer>()
    private val adapterScope = CoroutineScope(Dispatchers.Main)

    var currentPlayingPosition = -1
        set(value) {
            val previous = field
            field = value
            if (previous != value) {
                players[previous]?.pause()
                players[value]?.play()
            }
        }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ReelItem.VideoItem -> VIEW_TYPE_VIDEO
            is ReelItem.NativeAdItem -> VIEW_TYPE_AD
        }
    }

    @androidx.media3.common.util.UnstableApi
    inner class VideoViewHolder(val binding: ItemReelVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var lastTapTime = 0L

        fun bind(item: ReelItem.VideoItem, position: Int) {
            setupPlayer(item.url, position)

            binding.tvLike.text = item.likes.toString()
            binding.tvShare.text = item.downloads.toString()

            val prefs = getLikedPrefs()
            val isLiked = prefs.getBoolean("liked_${item.id}", false)
            setLikeState(isLiked)

            binding.playerView.setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastTapTime < 300) {
                    val liked = !prefs.getBoolean("liked_${item.id}", false)
                    prefs.edit().putBoolean("liked_${item.id}", liked).apply()
                    setLikeState(liked)
                    if (liked) showHeartAnimation()
                }
                lastTapTime = now
            }

            binding.ivLike.setOnClickListener {
                val liked = !prefs.getBoolean("liked_${item.id}", false)
                prefs.edit().putBoolean("liked_${item.id}", liked).apply()
                setLikeState(liked)
            }

            binding.cardDownload.setOnClickListener {
                onDownloadClick(item)
            }

            binding.ivShare.setOnClickListener {
                binding.shareProgress.visibility = View.VISIBLE
                shareVideoCoroutines(item.url, item.name) {
                    binding.shareProgress.visibility = View.GONE
                }
            }
        }

        @androidx.media3.common.util.UnstableApi
        private fun setupPlayer(url: String, position: Int) {
            val existing = players[position]
            if (existing != null) {
                binding.playerView.player = existing
                if (position == currentPlayingPosition) existing.play()
                return
            }

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .setDefaultRequestProperties(
                    mapOf(
                        "x-access-token" to AppConstants.REEL_ACCESS_TOKEN,
                        "Content-Type" to "application/json"
                    )
                )

            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

            val exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    setMediaItem(MediaItem.fromUri(url))
                    repeatMode = Player.REPEAT_MODE_ONE
                    playWhenReady = false
                    volume = 1f
                    prepare()
                }

            players[position] = exoPlayer
            binding.playerView.player = exoPlayer

            if (position == currentPlayingPosition) exoPlayer.play()
        }

        private fun setLikeState(liked: Boolean) {
            binding.ivLike.setColorFilter(if (liked) Color.RED else Color.WHITE)
        }

        private fun showHeartAnimation() {
            binding.ivAnimateLike.apply {
                setColorFilter(Color.RED)
                visibility = View.VISIBLE
                alpha = 1f
                scaleX = 0f
                scaleY = 0f
                animate().scaleX(1.5f).scaleY(1.5f).setDuration(200).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction {
                        animate().alpha(0f).setStartDelay(500).setDuration(300)
                            .withEndAction { visibility = View.GONE }.start()
                    }.start()
                }.start()
            }
        }

        private fun shareVideoCoroutines(url: String, title: String, onDone: () -> Unit) {
            adapterScope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        val tempFile = File(context.cacheDir, "share_${System.currentTimeMillis()}.mp4")
                        val conn = URL(url).openConnection() as HttpURLConnection
                        conn.connectTimeout = 15000
                        conn.readTimeout = 15000
                        conn.connect()
                        conn.inputStream.use { input -> tempFile.outputStream().use { input.copyTo(it) } }
                        tempFile
                    }

                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "video/mp4"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    onDone()
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_video)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e: Exception) {
                    onDone()
                    Log.d("shareVideo", "Error sharing video: ${e.message}")
                    Toast.makeText(context, "${context.getString(R.string.failed_to_share)}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun getLikedPrefs() =
            context.getSharedPreferences("reel_likes", Context.MODE_PRIVATE)
    }

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adContainer: FrameLayout? = view.findViewById(R.id.adContainer)

        fun bind(item: ReelItem.NativeAdItem) {
            val activity = context as? Activity ?: return
            if (item.adItem.isAdEnable && adContainer != null) {
                NativeAdsUtil.populateNativeAd(
                    isAdAllowed = true,
                    loadingAdView = R.layout.large_native_shimmer,
                    nativeAdView = R.layout.full_screen_native_ad,
                    adContainerView = adContainer,
                    nativeId = item.adItem.adId,
                    context = activity
                )
            } else {
                adContainer?.removeAllViews()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_VIDEO) {
            VideoViewHolder(ItemReelVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reel_ad, parent, false)
            AdViewHolder(view)
        }
    }

    fun isAdPosition(position: Int): Boolean {
        return items.getOrNull(position) is ReelItem.NativeAdItem
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ReelItem.VideoItem -> (holder as VideoViewHolder).bind(item, position)
            is ReelItem.NativeAdItem -> (holder as AdViewHolder).bind(item)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is VideoViewHolder) {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                players[position]?.release()
                players.remove(position)
            }
            holder.binding.playerView.player = null
        }
    }

    override fun getItemCount() = items.size

   /* fun updateData(
        newVideoItems: List<ReelItem.VideoItem>,
        adConfig: AdItem = RemoteConfigHelper.adIds.reelFullNative,
        adController: Int = getAdControllerValue(adConfig)
    ) {
        releaseAll()
        this.rawVideoItems = newVideoItems
        this.currentAdController = adController
        val newItems = buildReelListWithAds(newVideoItems, adConfig, adController)
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateAdController(
        adController: Int,
        adConfig: AdItem = RemoteConfigHelper.adIds.reelFullNative
    ) {
        if (this.currentAdController == adController && this.rawVideoItems.isNotEmpty()) return
        releaseAll()
        this.currentAdController = adController
        if (this.rawVideoItems.isNotEmpty()) {
            val newItems = buildReelListWithAds(rawVideoItems, adConfig, adController)
            this.items.clear()
            this.items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    fun setItems(newItems: List<ReelItem>) {
        releaseAll()
        this.rawVideoItems = newItems.filterIsInstance<ReelItem.VideoItem>()
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }*/

    fun resumeCurrent() { players[currentPlayingPosition]?.play() }
    fun pauseAll() { players.values.forEach { it.pause() } }
    fun releaseAll() { players.values.forEach { it.release() }; players.clear() }
}
