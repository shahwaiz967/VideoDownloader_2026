package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentHomeBinding
import com.codecx.videodownloader.features.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quickAccessButtons()

    }


    private fun quickAccessButtons(){
        binding.facebookButton.itemIcon.setImageResource(R.drawable.facebook_icon)
        binding.facebookButton.itemTitle.text = "FB Saver"
        binding.facebookButton.cardRoot.setOnClickListener { navigate("Facebook")}

        binding.whatsappButton.itemIcon.setImageResource(R.drawable.whatsapp_icon)
        binding.whatsappButton.itemTitle.text = "Status Saver"
        binding.whatsappButton.cardRoot.setOnClickListener { navigate("WhatsApp")}

        binding.instagramButton.itemIcon.setImageResource(R.drawable.instagram_icon)
        binding.instagramButton.itemTitle.text = "IG Saver"
        binding.instagramButton.cardRoot.setOnClickListener { navigate("Instagram")}

        binding.tiktokButton.itemIcon.setImageResource(R.drawable.tiktok_icon)
        binding.tiktokButton.itemTitle.text = "TikTok"
        binding.tiktokButton.cardRoot.setOnClickListener { navigate("TikTok")}

        binding.dailymotionButton.itemIcon.setImageResource(R.drawable.dailymotion_icon)
        binding.dailymotionButton.itemTitle.text = "dly Motion"
        binding.dailymotionButton.cardRoot.setOnClickListener { navigate("DailyMotion")}

        binding.twitterButton.itemIcon.setImageResource(R.drawable.twitter_icon)
        binding.twitterButton.itemTitle.text = "eX"
        binding.twitterButton.cardRoot.setOnClickListener { navigate("Twitter")}

        binding.pintresButton.itemIcon.setImageResource(R.drawable.pintres_icon)
        binding.pintresButton.itemTitle.text = "PinNest"
        binding.pintresButton.cardRoot.setOnClickListener { navigate("PinNest")}

        binding.threadButton.itemIcon.setImageResource(R.drawable.thread_icon)
        binding.threadButton.itemTitle.text = "Thredy"
        binding.threadButton.cardRoot.setOnClickListener { navigate("Thread")}

    }

    private fun navigate(title : String){
        val bundle = Bundle().apply {
            putString("name", title)
        }
        safeNavigate(R.id.socityFragment,bundle)
    }



}