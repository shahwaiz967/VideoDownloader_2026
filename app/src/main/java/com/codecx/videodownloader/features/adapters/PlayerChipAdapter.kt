package com.codecx.videodownloader.features.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.google.android.material.card.MaterialCardView

class PlayerChipAdapter(
    private val items: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<PlayerChipAdapter.ChipViewHolder>() {

    private var selectedPosition = 0

    inner class ChipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.chip_item)
        val tvChip: TextView = view.findViewById(R.id.chipText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chip_layout, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val title = items[position]
        holder.tvChip.text = title

        val context = holder.itemView.context

        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor))
            holder.cardView.strokeWidth = 0
            holder.tvChip.setTextColor(Color.WHITE)
        } else {

            holder.cardView.setCardBackgroundColor(Color.TRANSPARENT)
            holder.cardView.strokeWidth = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
            holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.primaryColor)
            holder.tvChip.setTextColor(ContextCompat.getColor(context, R.color.primaryColor)) // Or any neutral color
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition


            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onItemSelected(title)
        }
    }

    override fun getItemCount(): Int = items.size
}