package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.DownloadApiResponse
import com.codecx.videodownloader.data.models.MediaItem
import com.codecx.videodownloader.databinding.ItemDownloadBinding

class DownloadOptionAdapter(
    private val items: List<MediaItem>,
    private val data: DownloadApiResponse,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<DownloadOptionAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem) {

            binding.mediaIcon.setImageResource(
                if (item.isAudio()) R.drawable.audio_download_icon
                else R.drawable.media_download_icon
            )


            val context = binding.root.context
            binding.mediaTitle.text = when (item.quality) {
                "hd_no_watermark" -> context.getString(R.string.hd_no_watermark)
                "no_watermark"    -> context.getString(R.string.sd_no_watermark)
                "watermark"       -> context.getString(R.string.with_watermark)
                "audio"           -> context.getString(R.string.audio_only)
                else              -> item.quality ?: context.getString(R.string.download)
            }


            binding.tvFileSize.text = when {
                !item.size.isNullOrEmpty()        -> item.size
                item.dataSize != null             -> formatDataSize(item.dataSize)
                item.duration != null             -> formatDuration(item.duration.toInt())
                data.duration != null             -> formatDurationLong(data.duration.toLong())
                else                              -> ""
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    fun formatDataSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576     -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024         -> "%.2f KB".format(bytes / 1_024.0)
            else                   -> "$bytes B"
        }
    }

    fun formatDuration(ms: Int): String {
        val hours   = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000

        return when {
            hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
            else      -> "%d:%02d".format(minutes, seconds)
        }
    }
    fun formatDurationLong(ms: Long?): String {
        val hours   = ms!! / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000

        return when {
            hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
            else      -> "%d:%02d".format(minutes, seconds)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
       val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}