package com.codecx.videodownloader.features.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.ItemDownloadCompleteBinding
import com.codecx.videodownloader.databinding.ItemDownloadProgressBinding

class DownloadFragmentAdapter(private val isCompleted: Boolean, private val mContext: Context) :
    ListAdapter<DownloadModel, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object{
        private const val TYPE_ACTIVE = 0
        private const val TYPE_COMPLETED = 1
    }

    override fun getItemViewType(position: Int) =
        if (isCompleted) TYPE_COMPLETED else TYPE_ACTIVE




    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == TYPE_COMPLETED) {
            val binding = ItemDownloadCompleteBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            CompletedViewHolder(binding)
        } else {
            val binding = ItemDownloadProgressBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ActiveViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        when (holder) {
          is ActiveViewHolder -> holder.bind(item)
            is CompletedViewHolder -> holder.bind(item)
        }
    }

    inner class ActiveViewHolder(private val binding: ItemDownloadProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DownloadModel) {
            binding.tvTitle.text = item.title
            binding.tvProgress.text = "${item.progress}%"
            binding.progressBar.progress = item.progress
            binding.tvFileSize.text = item.fileSize

            if (item.thumbnailUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.thumbnailUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .centerCrop()
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder)
            }

        }
    }

    inner class CompletedViewHolder(private val binding: ItemDownloadCompleteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DownloadModel) {
            binding.tvTitle.text = item.title
            binding.tvFileSize.text = item.fileSize
            binding.tvPlatform.text = item.platform.ifEmpty { mContext.getString(R.string.downloaded) }

            if (item.thumbnailUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.thumbnailUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .centerCrop()
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder)
            }
            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("videoPath", item.localPath)
                bundle.putString("videoTitle", item.title)
                bundle.putBoolean("isAudio", item.isAudio)
                binding.root.safeNavigate(R.id.mediaPlayFragment, bundle)
            }

        }
    }



    class DiffCallback : DiffUtil.ItemCallback<DownloadModel>() {
        override fun areItemsTheSame(old: DownloadModel, new: DownloadModel) = old.id == new.id

        override fun areContentsTheSame(old: DownloadModel, new: DownloadModel) =
            old.progress == new.progress &&
                    old.status == new.status &&
                    old.fileSize == new.fileSize
    }
}

