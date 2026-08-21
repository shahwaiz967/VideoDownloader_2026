package com.codecx.videodownloader.features.ui.fragments

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.utils.ThemeManager
import com.codecx.videodownloader.data.utils.clearAppCache
import com.codecx.videodownloader.data.utils.isDefaultBrowser
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
        setupListeners()

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

    private fun setupListeners(){

        val packageName = requireContext().packageName
        with(binding){

            historyRow.root.setOnClickListener {
                safeNavigate(R.id.historyFragment)
            }
            bookMarkRow.root.setOnClickListener {
                safeNavigate(R.id.bookMarkFragment)
            }
            cacheRow.clearCache.setOnClickListener {
                requireContext().clearAppCache()
            }
            shareRow.root.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, packageName))
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
            }
            rateUSRow.root.setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                    )
                } catch (e: Exception) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                    )
                }
            }

            feedBaCKRow.root.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@videodownloader.app")
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
                }
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)))
            }
            privacyRow.root.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://appycreators12.blogspot.com/p/privacy-policy-video-downloader.html")
                    )
                )
            }

            swDefaultBrowser.setOnCheckedChangeListener { _, bool ->
                if (bool){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                        val roleManager = requireContext().getSystemService(RoleManager::class.java)
                        if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER) &&
                            !roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)
                        ) {

                            val intent = roleManager.createRequestRoleIntent(
                                RoleManager.ROLE_BROWSER
                            )

                            startActivityForResult(intent, 100)
                        }
                    }
                }
                else{
                    val intent = Intent(
                        Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                    )
                    startActivity(intent)
                }
            }
            swDarkMode.isChecked = ThemeManager.isDarkMode(requireContext())

            swDarkMode.setOnCheckedChangeListener { _, isChecked ->
                ThemeManager.setDarkMode(requireContext(), isChecked)
            }


        }
    }



}