package com.codecx.videodownloader.features.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.databinding.ItemChipLayoutBinding

class PlayerChipAdapter(
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<PlayerChipAdapter.ChipViewHolder>() {

    private val folders = mutableListOf<String>()
    private var selectedFolder = "All"

    fun submitFolders(newFolders: List<String>) {
        folders.clear()
        folders.add("All")
        folders.addAll(newFolders)
        notifyDataSetChanged()
    }

    fun setSelectedFolder(folder: String) {
        selectedFolder = folder
        notifyDataSetChanged()
    }

    inner class ChipViewHolder(private val binding: ItemChipLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: String) {
            binding.chipText.text = folder
            val context = binding.root.context

            if (folder == selectedFolder) {
                binding.chipItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor))
                binding.chipItem.strokeWidth = 0
                binding.chipText.setTextColor(Color.WHITE)
            } else {
                binding.chipItem.setCardBackgroundColor(Color.TRANSPARENT)
                binding.chipItem.strokeWidth = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
                binding.chipItem.strokeColor = ContextCompat.getColor(context, R.color.primaryColor)
                binding.chipText.setTextColor(ContextCompat.getColor(context, R.color.primaryColor))
            }

            binding.root.setOnClickListener {
                setSelectedFolder(folder)
                onItemSelected(folder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemChipLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount(): Int = folders.size
}
