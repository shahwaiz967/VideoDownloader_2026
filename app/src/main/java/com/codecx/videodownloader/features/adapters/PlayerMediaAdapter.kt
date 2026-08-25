package com.codecx.videodownloader.features.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.MediaModel
import com.codecx.videodownloader.data.utils.formatDuration
import com.codecx.videodownloader.data.utils.formatFileSize
import com.codecx.videodownloader.data.utils.showToast
import com.codecx.videodownloader.databinding.ItemAudioBinding
import com.codecx.videodownloader.databinding.ItemMediaRecyclerBinding
import java.io.File

class PlayerMediaAdapter(
    private val onItemClick: (MediaModel) -> Unit,
    private val onDeleteClick: (MediaModel) -> Unit
) : ListAdapter<MediaModel, RecyclerView.ViewHolder>(MediaDiffCallback()) {

    companion object {
        private const val TYPE_VIDEO = 0
        private const val TYPE_AUDIO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isAudio) TYPE_AUDIO else TYPE_VIDEO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_AUDIO) {
            AudioViewHolder(ItemAudioBinding.inflate(inflater, parent, false))
        } else {
            VideoViewHolder(ItemMediaRecyclerBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val media = getItem(position)
        when (holder) {
            is VideoViewHolder -> holder.bind(media)
            is AudioViewHolder -> holder.bind(media)
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
                    it.context.startActivity(Intent.createChooser(shareIntent, "Share via"))
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
            popup.menu.add(0, 1, 0, "Copy Path")
            //popup.menu.add(0, 2, 1, "Convert to Audio")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        val clipboard = anchor.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("path", media.path))
                        anchor.context.showToast("Path copied to clipboard")
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
            popup.menu.add(0, 1, 0, "Copy Path")
            popup.menu.add(0, 2, 1, "Delete")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        val clipboard = anchor.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("path", media.path))
                        anchor.context.showToast("Path copied to clipboard")
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

    class MediaDiffCallback : DiffUtil.ItemCallback<MediaModel>() {
        override fun areItemsTheSame(oldItem: MediaModel, newItem: MediaModel): Boolean {
            return oldItem.id == newItem.id && oldItem.isAudio == newItem.isAudio
        }

        override fun areContentsTheSame(oldItem: MediaModel, newItem: MediaModel): Boolean {
            return oldItem == newItem
        }
    }
}
