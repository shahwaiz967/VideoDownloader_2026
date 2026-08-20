package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentSettingBinding
import com.codecx.videodownloader.features.base.BaseFragment

class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.languageRow.root.setOnClickListener {
            safeNavigate(R.id.languageFragment)
        }
        rows()

    }

    private fun rows(){
        with(binding){

            historyRow.ivLanguageIcon.setImageResource(R.drawable.history_icon)
            historyRow.tvLanguage.text = "History"

            bookMarkRow.ivLanguageIcon.setImageResource(R.drawable.bookmark_icon)
            bookMarkRow.tvLanguage.text = "Bookmarks"

            cacheRow.ivLanguageIcon.setImageResource(R.drawable.ic_clear_cache)
            cacheRow.view3.visibility = View.GONE
            cacheRow.tvLanguage.text = "Clear Cache"
            cacheRow.clearCache.visibility = View.VISIBLE
            cacheRow.ivLanguageArrow.visibility = View.GONE

            searchEngine.tvSearchEngine.visibility = View.VISIBLE
            searchEngine.ivLanguageIcon.setImageResource(R.drawable.ic_search)
            searchEngine.tvLanguage.text = "Search Engine"

            rateUSRow.ivLanguageIcon.setImageResource(R.drawable.rate_us_icon)
            rateUSRow.tvLanguage.text = "Rate Us"

            shareRow.ivLanguageIcon.setImageResource(R.drawable.share_icon)
            shareRow.tvLanguage.text = "Share"

            feedBaCKRow.ivLanguageIcon.setImageResource(R.drawable.feedback_icon)
            feedBaCKRow.tvLanguage.text = "Feedback"

            privacyRow.ivLanguageIcon.setImageResource(R.drawable.privicy_policy_icon)
            privacyRow.tvLanguage.text = "Privacy Policy"
            privacyRow.view3.visibility = View.GONE
        }
    }


}