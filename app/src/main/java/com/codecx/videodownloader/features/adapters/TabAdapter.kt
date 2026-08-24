package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.databinding.TabItemLayotBinding

class TabAdapter(
    private var tabs: MutableList<TabModel>,
    private val onTabClick: (TabModel) -> Unit,
    private val onTabClose: (TabModel) -> Unit
) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    inner class TabViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val card: CardView = view.findViewById(R.id.cardTab)

        val icon: ImageView =
            view.findViewById(R.id.imgTabIcon)

        val title: TextView =
            view.findViewById(R.id.tvTabTitle)

        val url: TextView =
            view.findViewById(R.id.tvTabUrl)

        val closeBtn: ImageView =
            view.findViewById(R.id.btnCloseTab)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TabViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.tab_item_layot,
                parent,
                false
            )

        return TabViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TabViewHolder,
        position: Int
    ) {

        val tab = tabs[position]

        holder.title.text = tab.title
        holder.url.text = tab.url

        holder.icon.setImageResource(
            tab.favicon
        )

        holder.card.setCardBackgroundColor(
            if (tab.isSelected) {
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.primaryColor
                )
            } else {
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.white
                )
            }
        )

        holder.card.setOnClickListener {
            onTabClick(tab)
        }

        holder.closeBtn.setOnClickListener {

            if (tabs.size > 1) {
                onTabClose(tab)
            }
        }

        holder.closeBtn.visibility =
            if (tabs.size > 1) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun updateTabs(newTabs: List<TabModel>) {

        tabs.clear()
        tabs.addAll(newTabs)

        notifyDataSetChanged()
    }
}