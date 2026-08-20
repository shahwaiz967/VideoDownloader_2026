package com.codecx.videodownloader.features.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.LanguageModel
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentLanguageBinding
import com.codecx.videodownloader.features.adapters.LanguageAdapter
import com.codecx.videodownloader.features.base.BaseFragment


class LanguageFragment : BaseFragment<FragmentLanguageBinding>() {
    private lateinit var languages: List<LanguageModel>

    private lateinit var defaultTitle: TextView
    private lateinit var defaultRadioBtn: RadioButton
    private lateinit var defaultFlag: ImageView
    private lateinit var defaultCard : CardView

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
        defaultTitle = binding.defaultLanguageItem.tvLanguageName
        defaultRadioBtn = binding.defaultLanguageItem.radioButton
        defaultFlag = binding.defaultLanguageItem.ivFlag
        defaultCard = binding.defaultLanguageItem.mainCard

        defaultFlag.visibility = View.GONE
        defaultTitle.text = "English (US)"
        defaultTitle.setTextColor(Color.WHITE)
        defaultRadioBtn.isChecked = true
        defaultRadioBtn.buttonTintList = ColorStateList.valueOf(Color.WHITE)
        defaultCard.setCardBackgroundColor("#FA4501".toColorInt())
        languageRecycler()

    }

    private fun languageRecycler(){
        languages = listOf(
            LanguageModel(getString(R.string.english),    R.drawable.english,         "en"),
            LanguageModel("India", R.drawable.india, "hi"),
            LanguageModel(getString(R.string.arabic),     R.drawable.arabic,     "ar"),
            LanguageModel( "Bangla",     R.drawable.bangla,     "ar"),
            LanguageModel("Chinese (Simplified)",    R.drawable.chinese_simplified,      "zh"),
            LanguageModel("Chinese (Hong kong)",    R.drawable.chinese_hond_kong,      "zh"),
            LanguageModel(getString(R.string.chinese),    R.drawable.chine,      "zh"),
            LanguageModel("Dutch",    R.drawable.dutch,      "zh"),
            LanguageModel("French (Canada)",    R.drawable.canada,      "zh"),
            LanguageModel("French (France)",    R.drawable.french,      "zh"),
            LanguageModel("French ",    R.drawable.french,      "zh"),
            LanguageModel(getString(R.string.german),     R.drawable.germany,    "de"),
            LanguageModel("Greek",    R.drawable.greek,      "zh"),
            LanguageModel("Hindi", R.drawable.india, "hi"),
            LanguageModel("Indonesia", R.drawable.indonasia, "hi"),
            LanguageModel(getString(R.string.italian),    R.drawable.italy,      "it"),
            LanguageModel(getString(R.string.japanese),   R.drawable.japan,      "ja"),
            LanguageModel(getString(R.string.korean),     R.drawable.korea,     "ko"),
            LanguageModel("Malay", R.drawable.malay, "pt"),
            LanguageModel("Persian", R.drawable.persian, "pt"),
            LanguageModel("Polish", R.drawable.polish, "pt"),
            LanguageModel("Portuguese (Brazil)", R.drawable.brazil, "pt"),
            LanguageModel("Portuguese (Portugal)", R.drawable.portugus, "pt"),
            LanguageModel("Portuguese", R.drawable.portugus, "pt"),
            LanguageModel("Russian", R.drawable.russian, "pt"),
            LanguageModel("Spanish (Latin America)", R.drawable.spain, "pt"),
            LanguageModel("Spanish (Spain)", R.drawable.spain, "pt"),
            LanguageModel("Spanish (US)", R.drawable.spanish, "pt"),
            LanguageModel("Spanish", R.drawable.spain, "pt"),
            LanguageModel("Swedish", R.drawable.swidish, "pt"),
            LanguageModel("Tamil", R.drawable.india, "hi"),
            LanguageModel("Thai", R.drawable.thai, "hi"),
            LanguageModel(getString(R.string.turkey),     R.drawable.turky,     "tr"),
            LanguageModel("Ukranian", R.drawable.ukranian, "pt"),
            LanguageModel("Urdu" , R.drawable.pakistan, "hi"),
            LanguageModel("Vietnamese", R.drawable.vietnam, "pt")


        )
        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter = LanguageAdapter(languages, initialSelected = 0) {
        }
    }
}