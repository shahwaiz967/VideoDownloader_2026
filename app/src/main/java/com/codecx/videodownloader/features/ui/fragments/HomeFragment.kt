package com.codecx.videodownloader.features.ui.fragments

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.StringRes
import com.codecx.statussaver.features.activities.MainActivity
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.ads.utils.showInter
import com.codecx.videodownloader.data.ads.utils.showInterWithCounter
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.getClipboardText
import com.codecx.videodownloader.data.utils.isValidUrl
import com.codecx.videodownloader.data.utils.observeDownloadState
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.FragmentHomeBinding
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private var tabId: Long = -1L
    private val downloadViewModel: DownloadViewModel by activityViewModel()
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()
    private var isFirstActionDone = false


    override fun bindLayout(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): FragmentHomeBinding {
        tabId = arguments?.getLong(ARG_TAB_ID) ?: -1L
        return FragmentHomeBinding.inflate(inflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quickAccessButtons()
        observeDownload()
        bindAds()

        binding.btnPasteLink.setOnClickListener {
            handleFirstAction {
                val text = requireContext().getClipboardText()
                if (!text.isNullOrEmpty()) {
                    binding.etUrl.setText(text)
                    if (!text.isValidUrl()) {
                        makeText(
                            requireContext(),
                            getString(R.string.invalid_url),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@handleFirstAction
                    }
                    downloadViewModel.getDownloadInfo(text)
                } else {
                    makeText(
                        requireContext(),
                        getString(R.string.empty_clipboard), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnDownload.setOnClickListener {
            handleFirstAction {
                val url = binding.etUrl.text.toString().trim()
                if (url.isEmpty()) {
                    binding.etUrl.error = getString(R.string.enter_url)
                    return@handleFirstAction
                }
                downloadViewModel.getDownloadInfo(url)
            }
        }

    }

    private fun bindAds() {
        bindNative(RemoteConfigHelper.adIds.homeTopAd, false, binding.adLayoutTop)
        bindNative(RemoteConfigHelper.adIds.homeMidAd, false, binding.adLayoutMid)
        bindNative(RemoteConfigHelper.adIds.homeBottomAd, false, binding.adLayoutBottom)
    }

    private fun handleFirstAction(onComplete: () -> Unit) {
        if (!isFirstActionDone && RemoteConfigHelper.adIds.homeFirstClickInter) {
            isFirstActionDone = true
            showInter(
                interstitialHelper = interstitialHelper,
                stringId = RemoteConfigHelper.adIds.homeInter.adId,
                adAllowed = RemoteConfigHelper.adIds.homeInter.isAdEnable,
                onDismiss = onComplete,
                onFail = onComplete
            )
        } else {
            showInterWithCounter(
                interstitialHelper = interstitialHelper,
                adItem = RemoteConfigHelper.adIds.homeInter,
                onComplete = {
                    onComplete()
                }
            )

        }
    }

    private fun observeDownload() {

        observeDownloadState(
            viewModel = downloadViewModel,
            onLoading = {
                binding.btnDownload.isEnabled = false
                binding.btnDownload.text = getString(R.string.loading)
            },
            onSuccess = {
                binding.btnDownload.isEnabled = true
                binding.btnDownload.text = getString(R.string.download)
            },
            onReset = {
                binding.btnDownload.isEnabled = true
                binding.btnDownload.text = getString(R.string.download)
            }
        )
    }

    private fun checkClipboardAndPaste() {
        binding.etUrl.post {
            try {
                val clipboard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (!clipboard.hasPrimaryClip()) return@post

                val text =
                    clipboard.primaryClip?.getItemAt(0)?.coerceToText(requireContext())?.toString()
                        ?.trim() ?: return@post

                if (text.isNotEmpty() && isValidUrl(text)) {
                    binding.etUrl.setText(text)
                    binding.etUrl.setSelection(text.length)

                    downloadViewModel.getDownloadInfo(text)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun quickAccessButtons() {
        binding.facebookButton.itemIcon.setImageResource(R.drawable.facebook_icon)
        binding.facebookButton.itemTitle.text = getString(R.string.fb_saver)
        binding.facebookButton.cardRoot.setOnClickListener { navigate(R.string.facebook) }

        binding.whatsappButton.itemIcon.setImageResource(R.drawable.whatsapp_icon)
        binding.whatsappButton.itemTitle.text = getString(R.string.status_saver)
        binding.whatsappButton.cardRoot.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        binding.instagramButton.itemIcon.setImageResource(R.drawable.instagram_icon)
        binding.instagramButton.itemTitle.text = getString(R.string.ig_saver)
        binding.instagramButton.cardRoot.setOnClickListener { navigate(R.string.instagram) }

        binding.tiktokButton.itemIcon.setImageResource(R.drawable.tiktok_icon)
        binding.tiktokButton.itemTitle.text = getString(R.string.tiktok_title)
        binding.tiktokButton.cardRoot.setOnClickListener { navigate(R.string.tiktok) }

        binding.dailymotionButton.itemIcon.setImageResource(R.drawable.dailymotion_icon)
        binding.dailymotionButton.itemTitle.text = getString(R.string.dailymotion_title)
        binding.dailymotionButton.cardRoot.setOnClickListener { navigate(R.string.dailymotion) }

        binding.twitterButton.itemIcon.setImageResource(R.drawable.ic_x)
        binding.twitterButton.itemTitle.text = getString(R.string.twitter_title)
        binding.twitterButton.cardRoot.setOnClickListener { navigate(R.string.twitter) }

        binding.pintresButton.itemIcon.setImageResource(R.drawable.pintres_icon)
        binding.pintresButton.itemTitle.text = getString(R.string.pinnest_title)
        binding.pintresButton.cardRoot.setOnClickListener { navigate(R.string.pinnest) }

        binding.threadButton.itemIcon.setImageResource(R.drawable.ic_thredo)
        binding.threadButton.itemTitle.text = getString(R.string.thread_title)
        binding.threadButton.cardRoot.setOnClickListener { navigate(R.string.thread) }

    }


    private fun navigate(@StringRes title: Int) {
        val bundle = Bundle().apply {
            putInt("name", title)
            putLong("tabId", tabId)
        }

        handleFirstAction {
            safeNavigate(R.id.socityFragment, bundle)
        }
    }

    private fun isValidUrl(text: String): Boolean {
        return Patterns.WEB_URL.matcher(text)
            .matches() || text.startsWith("http://") || text.startsWith("https://") || text.startsWith(
            "www."
        )
    }


    companion object {
        private const val ARG_TAB_ID = "tab_id"
        fun newInstance(tabId: Long): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TAB_ID, tabId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity() as com.codecx.videodownloader.MainActivity

        if (activity.isComingFromBackground){
            activity.isComingFromBackground = false
            checkClipboardAndPaste()
        }
    }


}