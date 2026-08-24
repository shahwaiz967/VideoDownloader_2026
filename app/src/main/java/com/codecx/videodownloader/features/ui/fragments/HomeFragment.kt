package com.codecx.videodownloader.features.ui.fragments

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.states.ResultState
import com.codecx.videodownloader.data.utils.getClipboardText
import com.codecx.videodownloader.data.utils.isValidUrl
import com.codecx.videodownloader.data.utils.observeDownloadState
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.data.utils.showToast
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import com.codecx.videodownloader.databinding.FragmentHomeBinding
import com.codecx.videodownloader.features.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private var tabId: Long = -1L
    private val downloadViewModel: DownloadViewModel by activityViewModel()

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
        binding.btnPasteLink.setOnClickListener {
            val text = requireContext().getClipboardText()
            if (!text.isNullOrEmpty()) {
                binding.etUrl.setText(text)
                if (!text.isValidUrl()) {
                    makeText(requireContext(), getString(R.string.invalid_url), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                downloadViewModel.getDownloadInfo(text)
            } else {
                makeText(requireContext(),
                    getString(R.string.empty_clipboard), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDownload.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.isEmpty()) {
                binding.etUrl.error = "Enter URL"
                return@setOnClickListener
            }
            downloadViewModel.getDownloadInfo(url)
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
        binding.facebookButton.itemTitle.text = "FB Saver"
        binding.facebookButton.cardRoot.setOnClickListener { navigate(R.string.facebook) }

        binding.whatsappButton.itemIcon.setImageResource(R.drawable.whatsapp_icon)
        binding.whatsappButton.itemTitle.text = "Status Saver"
        binding.whatsappButton.cardRoot.setOnClickListener { navigate(R.string.whatsapp) }

        binding.instagramButton.itemIcon.setImageResource(R.drawable.instagram_icon)
        binding.instagramButton.itemTitle.text = "IG Saver"
        binding.instagramButton.cardRoot.setOnClickListener { navigate(R.string.instagram) }

        binding.tiktokButton.itemIcon.setImageResource(R.drawable.tiktok_icon)
        binding.tiktokButton.itemTitle.text = "TikTok"
        binding.tiktokButton.cardRoot.setOnClickListener { navigate(R.string.tiktok) }

        binding.dailymotionButton.itemIcon.setImageResource(R.drawable.dailymotion_icon)
        binding.dailymotionButton.itemTitle.text = "dly Motion"
        binding.dailymotionButton.cardRoot.setOnClickListener { navigate(R.string.dailymotion) }

        binding.twitterButton.itemIcon.setImageResource(R.drawable.twitter_icon)
        binding.twitterButton.itemTitle.text = "eX"
        binding.twitterButton.cardRoot.setOnClickListener { navigate(R.string.twitter) }

        binding.pintresButton.itemIcon.setImageResource(R.drawable.pintres_icon)
        binding.pintresButton.itemTitle.text = "PinNest"
        binding.pintresButton.cardRoot.setOnClickListener { navigate(R.string.pinnest) }

        binding.threadButton.itemIcon.setImageResource(R.drawable.thread_icon)
        binding.threadButton.itemTitle.text = "Thredy"
        binding.threadButton.cardRoot.setOnClickListener { navigate(R.string.thread) }

    }


    private fun navigate(@StringRes title: Int) {

        val bundle = Bundle().apply {
            putInt("name", title)
            putLong("tabId", tabId)
        }
        safeNavigate(R.id.socityFragment, bundle)
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



}