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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.models.LanguageModel
import com.codecx.videodownloader.data.models.PricesMenu
import com.codecx.videodownloader.data.utils.AndroidBillingHelper
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.BillingHelper
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.FragmentLanguageBinding
import com.codecx.videodownloader.features.adapters.LanguageAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.base.onBackPressWithAd
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class LanguageFragment : BaseFragment<FragmentLanguageBinding>() {

    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()
    private lateinit var languages: List<LanguageModel>

    private lateinit var defaultTitle: TextView
    private lateinit var defaultRadioBtn: RadioButton
    private lateinit var defaultFlag: ImageView
    private lateinit var defaultCard: CardView
    private var selectedPlan: PricesMenu? = null
    private val billingHelper: BillingHelper by lazy {
        AndroidBillingHelper.getInstance(requireContext())
    }

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentLanguageBinding {
        return FragmentLanguageBinding.inflate(inflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fromSettings = arguments?.getBoolean("fromSettings", false) ?: false
        binding.btnBack.visibility = if (fromSettings) View.VISIBLE else View.GONE

        onBackPressWithAd(interstitialHelper) {
            safePopBackStack()
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        bindNative(
            RemoteConfigHelper.adIds.languageTopNative,
            false,
            binding.topAdLayout
        )

        bindNative(
            RemoteConfigHelper.adIds.languageBottomNative,
            false,
            binding.adLayout
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billingHelper.offersUpdateListener.collectLatest { offers ->
                    val regularOffer = offers.find {
                        it.productId == AppConstants.SUB_PREMIUM_WEEKLY
                    }

                    if (regularOffer != null && regularOffer.price.isNotEmpty()) {
                        selectedPlan = regularOffer
                        binding.price.text = regularOffer.price.substringBefore(".")
                        binding.rs.visibility = View.GONE
                    } else {
                        selectedPlan = PricesMenu(
                            price = "",
                            microPrice = 0L,
                            currencyCode = "",
                            productId = AppConstants.SUB_PREMIUM_WEEKLY_3DAY_TRIAL
                        )
                    }

                    binding.premiumCard.setOnClickListener {
                        val offer = offers.find {
                            it.productId == AppConstants.SUB_PREMIUM_WEEKLY
                        }
                        if (offer != null)
                            billingHelper.launchBilling(offer)
                    }
                }
            }
        }

        binding.btnApply.setOnClickListener {
            val selectedLanguage =
                (binding.rvLanguages.adapter as LanguageAdapter).getSelectedLanguage()
            val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
            val currentLangCode =
                if (!currentLocales.isEmpty) currentLocales[0]?.toLanguageTag() else null

            var targetLocaleList: androidx.core.os.LocaleListCompat? = null

            if (selectedLanguage != null) {
                if (!selectedLanguage.code.equals(currentLangCode, ignoreCase = true)) {
                    targetLocaleList =
                        androidx.core.os.LocaleListCompat.forLanguageTags(selectedLanguage.code)
                }
            } else if (defaultRadioBtn.isChecked) {
                if (currentLangCode != null) {
                    targetLocaleList = androidx.core.os.LocaleListCompat.getEmptyLocaleList()
                }
            }

            safeNavigate(R.id.newPremiumFragment)

            if (targetLocaleList != null) {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(targetLocaleList)
            }
        }


        defaultTitle = binding.defaultLanguageItem.tvLanguageName
        defaultRadioBtn = binding.defaultLanguageItem.radioButton
        defaultFlag = binding.defaultLanguageItem.ivFlag
        defaultCard = binding.defaultLanguageItem.mainCard

        val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        val currentLangCode =
            if (!currentLocales.isEmpty) currentLocales[0]?.toLanguageTag() else null

        defaultFlag.visibility = View.GONE
        defaultTitle.text = getString(R.string.system_default)

        fun selectDefault() {
            defaultTitle.setTextColor(Color.WHITE)
            defaultRadioBtn.isChecked = true
            defaultRadioBtn.buttonTintList = ColorStateList.valueOf(Color.WHITE)
            defaultCard.setCardBackgroundColor("#FA4501".toColorInt())
            if (binding.rvLanguages.adapter != null) {
                (binding.rvLanguages.adapter as LanguageAdapter).clearSelection()
            }
        }

        fun deselectDefault() {
            defaultTitle.setTextColor(Color.BLACK)
            defaultRadioBtn.isChecked = false
            defaultRadioBtn.buttonTintList = ColorStateList.valueOf(Color.GRAY)
            defaultCard.setCardBackgroundColor(Color.WHITE)
        }

        binding.defaultLanguageItem.root.setOnClickListener {
            selectDefault()
        }

        if (currentLangCode == null) {
            selectDefault()
        } else {
            deselectDefault()
        }

        languageRecycler(currentLangCode, ::deselectDefault)
    }

    private fun languageRecycler(currentLangCode: String?, onItemSelected: () -> Unit) {
        languages = listOf(
            LanguageModel(getString(R.string.english), R.drawable.english, "en"),
            LanguageModel(getString(R.string.arabic), R.drawable.arabic, "ar"),
            LanguageModel(getString(R.string.bangla), R.drawable.bangla, "bn"),
            LanguageModel(
                getString(R.string.chinese_simplified),
                R.drawable.chinese_simplified,
                "zh-CN"
            ),
            LanguageModel(
                getString(R.string.chinese_hong_kong),
                R.drawable.chinese_hond_kong,
                "zh-HK"
            ),
            LanguageModel(getString(R.string.chinese), R.drawable.chine, "zh"),
            LanguageModel(getString(R.string.dutch), R.drawable.dutch, "nl"),
            LanguageModel(getString(R.string.french_canada), R.drawable.canada, "fr-CA"),
            LanguageModel(getString(R.string.french_france), R.drawable.french, "fr-FR"),
            LanguageModel(getString(R.string.french), R.drawable.french, "fr"),
            LanguageModel(getString(R.string.german), R.drawable.germany, "de"),
            LanguageModel(getString(R.string.greek), R.drawable.greek, "el"),
            LanguageModel(getString(R.string.hindi), R.drawable.india, "hi"),
            LanguageModel(getString(R.string.indonesian), R.drawable.indonasia, "id"),
            LanguageModel(getString(R.string.italian), R.drawable.italy, "it"),
            LanguageModel(getString(R.string.japanese), R.drawable.japan, "ja"),
            LanguageModel(getString(R.string.korean), R.drawable.korea, "ko"),
            LanguageModel(getString(R.string.malay), R.drawable.malay, "ms"),
            LanguageModel(getString(R.string.persian), R.drawable.persian, "fa"),
            LanguageModel(getString(R.string.polish), R.drawable.polish, "pl"),
            LanguageModel(getString(R.string.portuguese_brazil), R.drawable.brazil, "pt-BR"),
            LanguageModel(getString(R.string.portuguese_portugal), R.drawable.portugus, "pt-PT"),
            LanguageModel(getString(R.string.portuguese), R.drawable.portugus, "pt"),
            LanguageModel(getString(R.string.russian), R.drawable.russian, "ru"),
            LanguageModel(getString(R.string.spanish_latin_america), R.drawable.spain, "es-419"),
            LanguageModel(getString(R.string.spanish_spain), R.drawable.spain, "es-ES"),
            LanguageModel(getString(R.string.spanish_us), R.drawable.spanish, "es-US"),
            LanguageModel(getString(R.string.spanish), R.drawable.spain, "es"),
            LanguageModel(getString(R.string.swedish), R.drawable.swidish, "sv"),
            LanguageModel(getString(R.string.tamil), R.drawable.india, "ta"),
            LanguageModel(getString(R.string.thai), R.drawable.thai, "th"),
            LanguageModel(getString(R.string.turkish), R.drawable.turkish, "tr"),
            LanguageModel(getString(R.string.ukrainian), R.drawable.ukranian, "uk"),
            LanguageModel(getString(R.string.urdu), R.drawable.pakistan, "ur"),
            LanguageModel(getString(R.string.vietnamese), R.drawable.vietnam, "vi")
        )

        var initialSelectedIndex = -1
        if (currentLangCode != null) {
            initialSelectedIndex = languages.indexOfFirst {
                currentLangCode.equals(it.code, ignoreCase = true) ||
                        currentLangCode.replace("-Hans", "").equals(it.code, ignoreCase = true)
            }
            if (initialSelectedIndex == -1) {
                initialSelectedIndex = languages.indexOfFirst {
                    currentLangCode.startsWith(it.code.split("-")[0], ignoreCase = true)
                }
            }
        }

        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.adapter =
            LanguageAdapter(languages, initialSelected = initialSelectedIndex) {
                onItemSelected()
            }
    }
}