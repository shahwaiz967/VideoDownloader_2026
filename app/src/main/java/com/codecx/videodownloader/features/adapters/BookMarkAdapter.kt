package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.data.models.BrowserItem
import com.codecx.videodownloader.databinding.ItemHistoryLayoutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookMarkAdapter : ListAdapter<BrowserItem, BookMarkAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemHistoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BrowserItem) {
            binding.tvTitle.text = item.title.ifEmpty { item.url }
            binding.tvUrl.text = item.url
            binding.tvTime.text = formatTime(item.timestamp)
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<BrowserItem>() {
        override fun areItemsTheSame(old: BrowserItem, new: BrowserItem) = old.id == new.id
        override fun areContentsTheSame(old: BrowserItem, new: BrowserItem) = old == new
    }
}
