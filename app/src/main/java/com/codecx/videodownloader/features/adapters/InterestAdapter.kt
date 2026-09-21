package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.InterestModel
import com.codecx.videodownloader.databinding.ItemInterestBinding

class InterestAdapter(
    private val items: List<InterestModel>
) : RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

    inner class InterestViewHolder(val binding: ItemInterestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(interest: InterestModel) {
            binding.tvInterestName.text = interest.name
            binding.ivIcon.setImageResource(interest.iconRes)
            
            if (interest.isSelected) {
                binding.selectionCircle.setImageResource(R.drawable.ic_selected_circle)
            } else {
                binding.selectionCircle.setImageResource(R.drawable.ic_unselected_circle)
            }

            binding.root.setOnClickListener {
                interest.isSelected = !interest.isSelected
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val binding = ItemInterestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InterestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItems() = items.filter { it.isSelected }
}