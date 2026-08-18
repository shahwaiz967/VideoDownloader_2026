package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.LanguageModel
import com.codecx.videodownloader.databinding.LanguageItemLayoutBinding

class LanguageAdapter(
    private val languages: List<LanguageModel>,
    initialSelected: Int = -1,
    private val onLanguageSelected: (Int) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition = initialSelected

    inner class LanguageViewHolder(
        val binding: LanguageItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(language: LanguageModel, position: Int) {

            binding.tvLanguageName.text = language.name
            binding.ivFlag.setImageResource(language.flagResId)

            val isSelected = position == selectedPosition

            binding.radioButton.apply {
                isClickable = false
                isFocusable = false
                isChecked = isSelected

                buttonTintList = ContextCompat.getColorStateList(
                    binding.root.context,
                    if (isSelected) {
                        R.color.primaryColor
                    } else {
                        R.color.gray
                    }
                )
            }

            binding.root.setOnClickListener {

                val currentPosition = bindingAdapterPosition

                if (currentPosition == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }

                if (currentPosition == selectedPosition) {
                    return@setOnClickListener
                }

                val previousPosition = selectedPosition

                selectedPosition = currentPosition

                if (previousPosition >= 0) {
                    notifyItemChanged(previousPosition)
                }

                notifyItemChanged(currentPosition)

                onLanguageSelected(currentPosition)
            }
        }
    }
    fun clearSelection() {
        val previous = selectedPosition
        selectedPosition = -1
        if (previous >= 0) {
            notifyItemChanged(previous)
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LanguageViewHolder {

        val binding = LanguageItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: LanguageViewHolder,
        position: Int
    ) {
        holder.bind(
            languages[position],
            position
        )
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    fun getSelectedLanguage(): LanguageModel? {
        return if (
            selectedPosition >= 0 &&
            selectedPosition < languages.size
        ) {
            languages[selectedPosition]
        } else {
            null
        }
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    fun setSelectedPosition(position: Int) {

        if (position !in languages.indices) {
            return
        }

        val previousPosition = selectedPosition

        selectedPosition = position

        if (previousPosition >= 0) {
            notifyItemChanged(previousPosition)
        }

        notifyItemChanged(position)
    }
}