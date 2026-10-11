package com.codecx.videodownloader.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codecx.videodownloader.databinding.ItemOnboardingBinding

class onBoardingAdapter(private val list: List<Int>) :
    RecyclerView.Adapter<onBoardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(val binding: ItemOnboardingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(
            ItemOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        Glide.with(holder.binding.ivOnboarding).load(list[position])
            .into(holder.binding.ivOnboarding)
    }
}