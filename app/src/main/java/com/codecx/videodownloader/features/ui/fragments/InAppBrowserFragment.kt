package com.codecx.videodownloader.features.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.data.utils.SearchEngineManager
import com.codecx.videodownloader.data.utils.showToast
import com.codecx.videodownloader.databinding.FragmentInAppBrowserBinding
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.ui.viewmodel.HistoryBookViewM
import com.codecx.videodownloader.features.ui.viewmodel.TabViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class InAppBrowserFragment :
    BaseFragment<FragmentInAppBrowserBinding>() {

    private val tabViewModel: TabViewModel by activityViewModel()
    private val viewModel: HistoryBookViewM by viewModel()

    private var currentTabId: String? = null

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentInAppBrowserBinding {
        return FragmentInAppBrowserBinding.inflate(
            inflater,
            container,
            false
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        currentTabId =
            tabViewModel.getSelectedTab()?.id

        setupWebView()
        setupControls()
        setupBackPress()

        loadSelectedTab()
    }

    private fun loadSelectedTab() {
        val selectedTab =
            tabViewModel.getSelectedTab()
        val intentUri = arguments?.getString("url")
        val searchEngine = SearchEngineManager.getSelectedSearchEngine(requireContext())
        val resolvedUrl = intentUri ?: run {
            if (selectedTab?.url == "about:blank" || selectedTab == null) {
                searchEngine.homeUrl
            } else
                selectedTab.url
        }
        loadUrl(resolvedUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        binding.webView.settings.apply {

            javaScriptEnabled = true
            domStorageEnabled = true

            loadWithOverviewMode = true
            useWideViewPort = true

            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)

            allowContentAccess = true
            allowFileAccess = true

            mixedContentMode =
                WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        binding.webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url =
                        request?.url?.toString()
                            ?: return false

                    return if (
                        url.startsWith("http://") ||
                        url.startsWith("https://")
                    ) {

                        view?.loadUrl(url)
                        false

                    } else {

                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                                )
                            )
                        } catch (_: Exception) {
                        }

                        true
                    }
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    super.onPageFinished(view, url)

                    url ?: return
                    val title = view?.title ?: url
                    
                    binding.etUrl.setText(url)
                    viewModel.addHistory(title, url)

                    updateCurrentTab(
                        title = view?.title ?: url,
                        url = url
                    )


                    updateNavButtons()
                }
            }

        binding.webView.webChromeClient =
            object : WebChromeClient() {

                override fun onProgressChanged(
                    view: WebView?,
                    newProgress: Int
                ) {

                    binding.progressBar.progress =
                        newProgress

                    binding.progressBar.visibility =
                        if (newProgress < 100) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }

                override fun onReceivedTitle(
                    view: WebView?,
                    title: String?
                ) {

                    binding.tvTitle.text =
                        title ?: "Loading..."

                    updateCurrentTab(
                        title = title
                    )
                }
            }
    }

    private fun setupControls() {

        binding.btnClose.setOnClickListener {
           navigateBack()
        }

        binding.btnBack.setOnClickListener {

            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                navigateBack()
            }
        }

        binding.btnBookmark.setOnClickListener {
            val url = binding.webView.url
            val title = binding.webView.title ?: url ?: "No Title"
            if (url != null) {
                viewModel.addBookmark(title, url)
                requireContext().showToast("Bookmarked")
            }
        }



        binding.etUrl.setOnKeyListener { _, keyCode, event ->

            if (
                keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN
            ) {

                var input =
                    binding.etUrl.text
                        .toString()
                        .trim()

                if (
                    !input.startsWith("http://") &&
                    !input.startsWith("https://")
                ) {

                    input =
                        if (
                            input.contains(".") &&
                            !input.contains(" ")
                        ) {

                            "https://$input"

                        } else {
                            val searchEngine = SearchEngineManager.getSelectedSearchEngine(requireContext())
                            "${searchEngine.searchUrl}${
                                Uri.encode(input)
                            }"
                        }
                }

                loadUrl(input)

                true

            } else {
                false
            }
        }
    }

    private fun setupBackPress() {

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {

                    override fun handleOnBackPressed() {

                        if (binding.webView.canGoBack()) {
                            binding.webView.goBack()
                        } else {
                            navigateBack()
                        }
                    }
                }
            )
    }

    private fun loadUrl(url: String) {

        binding.webView.loadUrl(url)
        binding.etUrl.setText(url)
    }

    private fun updateCurrentTab(
        title: String? = null,
        url: String? = null
    ) {

        val tabId = currentTabId ?: return

        tabViewModel.updateTab(
            tabId = tabId,
            title = title,
            url = url
        )
    }

    private fun updateNavButtons() {

        val canGoBack =
            binding.webView.canGoBack()


        binding.btnBack.isEnabled =
            canGoBack

        binding.btnBack.alpha =
            if (canGoBack) 1f else 0.4f

    }

    private fun navigateBack() {

        findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroyView() {
        binding.webView.stopLoading()
        binding.webView.destroy()
        super.onDestroyView()
    }
}