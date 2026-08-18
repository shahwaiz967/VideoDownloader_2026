package com.codecx.videodownloader.features.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.LanguageModel
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentLanguageBinding
import com.codecx.videodownloader.features.adapters.LanguageAdapter
import com.codecx.videodownloader.features.base.BaseFragment


class LanguageFragment : BaseFragment<FragmentLanguageBinding>() {
    private lateinit var languages: List<LanguageModel>
    private lateinit var languageAdapter: LanguageAdapter

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(inflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnApply.setOnClickListener {
           safeNavigate(R.id.premiumFragment)
        }
        languageRecycler()


    }

    private fun languageRecycler(){
        languages = listOf(
            LanguageModel(getString(R.string.english),    R.drawable.english,         "en"),
            LanguageModel(getString(R.string.korean),     R.drawable.korea,     "ko"),
            LanguageModel(getString(R.string.turkey),     R.drawable.turky,     "tr"),
            LanguageModel(getString(R.string.arabic),     R.drawable.arabic,     "ar"),
            LanguageModel(getString(R.string.japanese),   R.drawable.japan,      "ja"),
            LanguageModel(getString(R.string.italian),    R.drawable.italy,      "it"),
            LanguageModel(getString(R.string.german),     R.drawable.germany,    "de"),
            LanguageModel(getString(R.string.chinese),    R.drawable.chine,      "zh"),
        )
        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = LanguageAdapter(languages, initialSelected = 0) {
        }
    }
}