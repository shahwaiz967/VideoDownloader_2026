package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.databinding.TabItemLayotBinding

class TabAdapter(
    private val onTabClick: (TabModel) -> Unit,
    private val onCloseClick: (TabModel) -> Unit
) : RecyclerView.Adapter<TabAdapter.ViewHolder>() {

    private val tabs = mutableListOf<TabModel>()

    inner class ViewHolder(
        private val binding: TabItemLayotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tab: TabModel) {

            binding.tvTabTitle.text = tab.title
            binding.tvTabUrl.text = tab.url

            if (tab.isSelected) {

                binding.cardTab.strokeWidth = 2

                binding.cardTab.strokeColor =
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.primaryColor
                    )

            } else {

                binding.cardTab.strokeWidth = 2

                binding.cardTab.strokeColor =
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
            }

            binding.root.setOnClickListener {
                onTabClick(tab)
            }

            binding.btnCloseTab.setOnClickListener {
                onCloseClick(tab)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = TabItemLayotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(tabs[position])
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun submitList(newTabs: List<TabModel>) {

        tabs.clear()
        tabs.addAll(newTabs)

        notifyDataSetChanged()
    }
}