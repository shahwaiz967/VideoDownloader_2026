package com.codecx.videodownloader.features.adapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.nativeAds.NativeAdsUtil
import com.codecx.videodownloader.data.models.AdItem
import com.codecx.videodownloader.data.models.MediaModel
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.formatDuration
import com.codecx.videodownloader.data.utils.formatFileSize
import com.codecx.videodownloader.data.utils.showToast
import com.codecx.videodownloader.databinding.ItemAudioBinding
import com.codecx.videodownloader.databinding.ItemMediaRecyclerBinding
import java.io.File

sealed class PlayerAdapterItem {
    data class Media(val media: MediaModel) : PlayerAdapterItem()
    data class Ad(val adItem: AdItem = RemoteConfigHelper.adIds.videoPlayerNative) : PlayerAdapterItem()
}

class PlayerMediaAdapter(
    private val onItemClick: (MediaModel) -> Unit,
    private val onDeleteClick: (MediaModel) -> Unit
) : ListAdapter<PlayerAdapterItem, RecyclerView.ViewHolder>(PlayerAdapterItemDiffCallback()) {

    companion object {
        const val TYPE_VIDEO = 0
        const val TYPE_AUDIO = 1
        const val TYPE_AD_VIEW = 2
    }

    fun getAdControllerValue(adConfig: AdItem = RemoteConfigHelper.adIds.videoPlayerNative): Int {
        return when {
            RemoteConfigHelper.adIds.playerNativeFrequency > 0 -> RemoteConfigHelper.adIds.playerNativeFrequency
            else -> RemoteConfigHelper.adIds.adFrequency
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is PlayerAdapterItem.Media -> if (item.media.isAudio) TYPE_AUDIO else TYPE_VIDEO
            is PlayerAdapterItem.Ad -> TYPE_AD_VIEW
            null -> TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_AUDIO -> AudioViewHolder(ItemAudioBinding.inflate(inflater, parent, false))
            TYPE_AD_VIEW -> {
                val view = inflater.inflate(R.layout.item_video_ad, parent, false)
                AdViewHolder(view)
            }
            else -> VideoViewHolder(ItemMediaRecyclerBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlayerAdapterItem.Media -> {
                when (holder) {
                    is VideoViewHolder -> holder.bind(item.media)
                    is AudioViewHolder -> holder.bind(item.media)
                }
            }
            is PlayerAdapterItem.Ad -> {
                (holder as? AdViewHolder)?.bind(item.adItem)
            }
            null -> {}
        }
    }

    fun submitMediaList(
        list: List<MediaModel>,
        adConfig: AdItem = RemoteConfigHelper.adIds.videoPlayerNative
    ) {
        val items = mutableListOf<PlayerAdapterItem>()
        val adController = getAdControllerValue(adConfig)
        if (adConfig.isAdEnable && adController > 0) {
            var count = 0
            for (media in list) {
                items.add(PlayerAdapterItem.Media(media))
                count++
                if (count % adController == 0) {
                    items.add(PlayerAdapterItem.Ad(adConfig))
                }
            }
        } else {
            items.addAll(list.map { PlayerAdapterItem.Media(it) })
        }
        submitList(items)
    }

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adContainer: FrameLayout? = view.findViewById(R.id.adContainer)

        fun bind(adItem: AdItem) {
            val activity = itemView.context as? Activity ?: return
            if (adItem.isAdEnable && adContainer != null) {
                NativeAdsUtil.populateNativeAd(
                    isAdAllowed = true,
                    loadingAdView = R.layout.native_banner_shimmer,
                    nativeAdView = R.layout.native_banner,
                    adContainerView = adContainer,
                    nativeId = adItem.adId,
                    context = activity
                )
            } else {
                adContainer?.removeAllViews()
            }
        }
    }

    inner class VideoViewHolder(private val binding: ItemMediaRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(media: MediaModel) {
            binding.tvDuration.text = media.duration.formatDuration()

            Glide.with(binding.root.context)
                .load(media.path)
                .centerCrop()
                .placeholder(R.drawable.preview_image)
                .into(binding.ivThumbnail)

            binding.root.setOnClickListener { onItemClick(media) }
            binding.ivMore.setOnClickListener { showPopupMenu(it, media) }
            binding.ivShare.setOnClickListener {
                try {
                    val shareUri = getFileUri(media) ?: return@setOnClickListener
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (media.isAudio) "audio/*" else "video/*"
                        putExtra(Intent.EXTRA_STREAM, shareUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    it.context.startActivity(Intent.createChooser(shareIntent, it.context.getString(R.string.share_via)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            binding.ivDelete.setOnClickListener {
                onDeleteClick(media)
            }
        }

        private fun getFileUri(item: MediaModel): Uri? {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Uri.parse(item.path)
                } else {
                    val file = File(item.path)
                    if (!file.exists()) return null
                    androidx.core.content.FileProvider.getUriForFile(
                        binding.root.context,
                        "${binding.root.context.packageName}.fileprovider",
                        file
                    )
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun showPopupMenu(anchor: View, media: MediaModel) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menu.add(0, 1, 0, anchor.context.getString(R.string.copy_path))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        val clipboard = anchor.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("path", media.path))
                        anchor.context.showToast(anchor.context.getString(R.string.path_copied_to_clipboard))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    inner class AudioViewHolder(private val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(media: MediaModel) {
            binding.tvTitle.text = media.title
            binding.tvDuration.text = media.duration.formatDuration()
            binding.tvSize.text = media.size.formatFileSize()

            binding.root.setOnClickListener { onItemClick(media) }
            binding.ivMore.setOnClickListener { showPopupMenu(it, media) }
        }

        private fun showPopupMenu(anchor: View, media: MediaModel) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menu.add(0, 1, 0, anchor.context.getString(R.string.copy_path))
            popup.menu.add(0, 2, 1, anchor.context.getString(R.string.delete))
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        val clipboard = anchor.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("path", media.path))
                        anchor.context.showToast(anchor.context.getString(R.string.path_copied_to_clipboard))
                        true
                    }
                    2 -> {
                        onDeleteClick(media)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    fun clearList() {
        submitList(null)
    }

    class PlayerAdapterItemDiffCallback : DiffUtil.ItemCallback<PlayerAdapterItem>() {
        override fun areItemsTheSame(oldItem: PlayerAdapterItem, newItem: PlayerAdapterItem): Boolean {
            return when {
                oldItem is PlayerAdapterItem.Media && newItem is PlayerAdapterItem.Media ->
                    oldItem.media.id == newItem.media.id && oldItem.media.isAudio == newItem.media.isAudio
                oldItem is PlayerAdapterItem.Ad && newItem is PlayerAdapterItem.Ad -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: PlayerAdapterItem, newItem: PlayerAdapterItem): Boolean {
            return oldItem == newItem
        }
    }
}
