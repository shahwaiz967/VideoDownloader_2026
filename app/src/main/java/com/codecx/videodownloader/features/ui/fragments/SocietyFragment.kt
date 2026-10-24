package com.codecx.videodownloader.features.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.codecx.videodownloader.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.getClipboardText
import com.codecx.videodownloader.data.utils.isAdultUrl
import com.codecx.videodownloader.data.utils.isValidUrl
import com.codecx.videodownloader.data.utils.observeDownloadState
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.FragmentSocityBinding
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.base.onBackPressWithAd
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class SocietyFragment : BaseFragment<FragmentSocityBinding>() {

    private val downloadViewModel : DownloadViewModel by activityViewModel()
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSocityBinding {
        return FragmentSocityBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeDownload()

        bindNative(
            RemoteConfigHelper.adIds.societyBanner,
            false,
            binding.adLayout
        )
        bindNative(
            RemoteConfigHelper.adIds.societyNativeTop,
            false,
            binding.adTopLayout
        )


        binding.btnPasteLink.setOnClickListener {
            val text = requireContext().getClipboardText()
            if (!text.isNullOrEmpty()) {
                binding.etUrl.setText(text)
                if (!text.isValidUrl()) {
                    makeText(requireContext(), getString(R.string.invalid_url), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (text.isAdultUrl()) {
                    makeText(requireContext(), getString(R.string.adult_site_restricted), Toast.LENGTH_SHORT).show()
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
                binding.etUrl.error = getString(R.string.enter_url)
                return@setOnClickListener
            }
            if (!url.isValidUrl()) {
                makeText(requireContext(), getString(R.string.invalid_url), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (url.isAdultUrl()) {
                makeText(requireContext(), getString(R.string.adult_site_restricted), Toast.LENGTH_SHORT).show()
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



    private fun setupToolbar() {
        val headerText = arguments?.getInt("name", 0) ?: 0

        if (headerText != 0) {
            binding.tvTitle.text = getString(headerText)
        }

        onBackPressWithAd(interstitialHelper) {
            safePopBackStack()
        }

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        when (headerText) {
            R.string.dailymotion -> {
                binding.ivStepImage1.setImageResource(R.drawable.dailymotion_preview_image)
                binding.tvDescription.text = getString(R.string._1_open_dailymotion_amp_tap)
            }

            R.string.pinnest -> {
                binding.step3Include.step3Image.setImageResource(R.drawable.pinteres_step3_image)
                binding.ivStepImage1.setImageResource(R.drawable.mobile_image)
            }

            R.string.twitter -> {
                binding.step3Include.step3Image.setImageResource(R.drawable.twitter_step3_image)
                binding.ivStepImage1.setImageResource(R.drawable.teitter_preview_image)
                binding.tvDescription.text = getString(R.string._1_open_twitter_amp_tap)
            }

            R.string.thread -> {
                binding.ivStepImage1.setImageResource(R.drawable.thread_preview_image)
                binding.tvDescription.text = getString(R.string._1_open_thread_amp_tap)
            }

            R.string.tiktok -> {
                binding.step1Card.visibility = View.GONE
                binding.step2Card.visibility = View.GONE
                binding.step3Card.visibility = View.GONE
                binding.tiktokstep1Card.visibility = View.VISIBLE
                binding.tiktokstep2Card.visibility = View.VISIBLE
            }

            R.string.facebook -> {
                binding.step3Include.step3Image.setImageResource(R.drawable.facebook_insta_step3_image)
                binding.ivStepImage1.setImageResource(R.drawable.facebook_step1_image)
                binding.tvDescription.text = getString(R.string._1_open_facebook)
                binding.tvDescription1.visibility = View.GONE
            }

            R.string.instagram -> {
                binding.step3Include.step3Image.setImageResource(R.drawable.facebook_insta_step3_image)
                binding.ivStepImage1.setImageResource(R.drawable.facebook_step1_image)
                binding.tvDescription.text = getString(R.string._1_open_instagram)
                binding.tvDescription1.visibility = View.GONE
                binding.openInstaCard.root.visibility = View.VISIBLE
            }

        }
        binding.openInstaCard.openInstaButton.setOnClickListener {
            openInstagramProfile(requireContext())
        }
    }

    fun openInstagramProfile(context: Context) {
        val appUri = Uri.parse("instagram://user?username=")
        val webUri = Uri.parse("https://instagram.com/")

        val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
            `package` = "com.instagram.android"
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {

            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }


}