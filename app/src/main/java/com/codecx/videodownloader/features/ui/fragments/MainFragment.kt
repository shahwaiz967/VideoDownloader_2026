package com.codecx.videodownloader.features.ui.fragments

import android.app.Dialog
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.googleads.InterstitialAdHelper
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.ads.utils.showInterWithCounter
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.databinding.DialogExitBinding
import com.codecx.videodownloader.databinding.FragmentMainBinding
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.base.onBackPressWithAd
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import com.codecx.videodownloader.features.ui.viewmodel.TabViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.perf.config.RemoteConfigManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File



@androidx.media3.common.util.UnstableApi
class MainFragment : BaseFragment<FragmentMainBinding>() {

    private val tabViewModel: TabViewModel by activityViewModel()
    private val downloadViewModel: DownloadViewModel by activityViewModel()
    private val STORAGE_FOLDER = "VideoDownloader"

    private lateinit var ivThumbnail: ImageView


    private var currentTab = TAB_HOME
    private val interstitialHelper = InterstitialAdHelper.InterstitialHelper()


    companion object {
        const val TAB_HOME = 0
        const val TAB_DOWNLOADS = 1
        const val TAB_REELS = 2
        const val TAB_PLAYER = 3

    }

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabCard.setOnClickListener {
            showTabsSheet()
        }
        navigation()
        refreshTabBadge()
        initStartupPage()
        observeDownloadCompletion()
        observeDownloadStart()

        onBackPressWithAd(interstitialHelper){
            showExitDialog()
        }
        binding.start3DaysTrailBtn.setOnClickListener {
            safeNavigate(R.id.freeTrailFragment)
        }
    }

    private fun showExitDialog(){
        val ctx = context ?: return
        val dialog = Dialog(ctx)
        val binding = DialogExitBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        bindNative(
            RemoteConfigHelper.adIds.exitDialogNative,
            false,
            binding.adLayoutTop
        )
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnExit.setOnClickListener {
            dialog.dismiss()
            activity?.finish()
        }

        dialog.show()

        val width = resources.displayMetrics.widthPixels -
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp)

        dialog.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    private fun initStartupPage() {
        val currentFragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment != null) {
            updateTab(currentFragment)
        } else {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (!isAdded || isStateSaved || childFragmentManager.isStateSaved) return

        val current = childFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (current != null && current::class == fragment::class) {
            return
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()

        updateTab(fragment)
    }

    fun updateTab(fragment: Fragment) {
        val tab = when (fragment) {
            is HomeFragment -> TAB_HOME
            is DownloadFragment -> TAB_DOWNLOADS
            is PlayerFragment -> TAB_PLAYER
            is ReelFragment -> TAB_REELS
            else -> currentTab
        }
        selectTab(tab)
    }

    private fun observeDownloadCompletion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadViewModel.downloadCompleteEvent.collectLatest { downloadItem ->
                    val ctx = context ?: return@collectLatest
                    Toast.makeText(ctx, getString(R.string.download_complete), Toast.LENGTH_SHORT).show()
                    showDownloadCompleteSheet(downloadItem)
                }
            }
        }
    }

    private fun observeDownloadStart() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadViewModel.downloadStartedEvent.collectLatest {
                    loadFragment(DownloadFragment())
                }
            }
        }
    }

    private fun selectTab(tab: Int) {
        currentTab = tab

        setTabActive(
            binding.tvNavHome,
            tab == TAB_HOME
        )

        setTabActive(
            binding.tvNavDownloads,
            tab == TAB_DOWNLOADS
        )

        setTabActive(
            binding.tvNavReels,
            tab == TAB_REELS
        )

        setTabActive(
            binding.tvNavPlayer,
            tab == TAB_PLAYER
        )
    }

    private fun setTabActive(
        label: TextView,
        isActive: Boolean
    ) {
        val ctx = context ?: return
        val color = ContextCompat.getColor(
            ctx,
            R.color.white
        )

        label.setTextColor(color)

        label.setTypeface(
            null,
            if (isActive) Typeface.BOLD
            else Typeface.NORMAL
        )
    }

    private fun showTabsSheet() {
        if (!isAdded || isDetached) return
        TabsBottomSheetFragment(
            onTabSelected = { tab ->

                tabViewModel.selectTab(tab.id)
                if (tab.url != "about:blank") {
                    openInAppBrowser(tab.url, false)
                } else {
                    safeNavigate(R.id.inAppBrowserFragment)
                }
            },
            onTabsChanged = {
                refreshTabBadge()
            }
        ).show(
            childFragmentManager,
            TabsBottomSheetFragment.TAG
        )
    }

    private fun refreshTabBadge() {
        (binding.ivTabBadge as? TextView)?.text = tabViewModel.getTabCount().toString()
    }


    private fun navigation() {
        binding.navHome.setOnClickListener {
            handleNavigationAd { loadFragment(HomeFragment()) }
        }
        binding.navDownloads.setOnClickListener {
            handleNavigationAd { loadFragment(DownloadFragment()) }
        }
        binding.navPlayer.setOnClickListener {
            handleNavigationAd { loadFragment(PlayerFragment()) }
        }
        binding.navReels.setOnClickListener {
            handleNavigationAd { safeNavigate(R.id.reelFragment) }
        }
        binding.btnReels.setOnClickListener {
            handleNavigationAd { safeNavigate(R.id.reelFragment) }
        }
        binding.ivPremium.setOnClickListener {
            safeNavigate(R.id.newPremiumFragment)
        }
        binding.ivSettings.setOnClickListener {
            safeNavigate(R.id.settingFragment)
        }
    }


    private fun handleNavigationAd(onComplete: () -> Unit) {
        showInterWithCounter(
            interstitialHelper = interstitialHelper,
            adItem = RemoteConfigHelper.adIds.homeInter,
            onComplete = onComplete
        )
    }



    fun openInAppBrowser(
        url: String,
        createNewTab: Boolean = false
    ) {

        if (createNewTab) {
            val newTab = tabViewModel.addTab()

            tabViewModel.updateTab(
                tabId = newTab.id,
                title = url,
                url = url
            )

            refreshTabBadge()
        } else {

            val selectedTab = tabViewModel.getSelectedTab()

            selectedTab?.let {
                tabViewModel.updateTab(
                    tabId = it.id,
                    url = url
                )
            }
        }

        val bundle = Bundle().apply {
            putString("url", url)
        }

        safeNavigate(
            R.id.inAppBrowserFragment,
            bundle
        )
    }


    private fun showDownloadCompleteSheet(item: DownloadModel) {
        val ctx = context ?: return
        val bottomSheet = BottomSheetDialog(ctx)
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheet.setContentView(R.layout.download_complete_sheet)


        val btnOpenFolder = bottomSheet.findViewById<MaterialCardView>(R.id.btnOpenFolder)
        val btnShare = bottomSheet.findViewById<MaterialCardView>(R.id.btnShare)
        val btnView = bottomSheet.findViewById<MaterialCardView>(R.id.btnView)
        val closeBtn = bottomSheet.findViewById<ImageView>(R.id.close_btn)
        ivThumbnail = bottomSheet.findViewById(R.id.ivDownloadCompleteThumbnail)!!
        val tvTitle = bottomSheet.findViewById<TextView>(R.id.tvTitle)
        val tvAuthor = bottomSheet.findViewById<TextView>(R.id.tvAuthor)

        tvTitle?.text = item.title
        tvAuthor?.text = "${getString(R.string.just_now)} · ${item.fileSize}"


        if (item.thumbnailUrl.isNotEmpty()) {
            Glide.with(ctx)
                .load(item.thumbnailUrl)
                .placeholder(R.drawable.ic_video_placeholder)
                .into(ivThumbnail)
        }

        closeBtn?.setOnClickListener {
            bottomSheet.dismiss()
        }
        btnOpenFolder?.setOnClickListener {
            openDownloadFolder(item)
        }

        btnShare?.setOnClickListener {
            try {
                val shareUri = getFileUri(item) ?: return@setOnClickListener
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = if (item.isAudio) "audio/*" else "video/*"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        btnView?.setOnClickListener {
            try {
                val bundle = Bundle()
                bundle.putString("videoPath", item.localPath)
                bundle.putString("videoTitle", item.title)
                bundle.putBoolean("isAudio", item.isAudio)
                safeNavigate(R.id.mediaPlayFragment, bundle)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            bottomSheet.dismiss()
        }

        bottomSheet.window?.setBackgroundDrawableResource(android.R.color.transparent)
        bottomSheet.show()
    }

    private fun getFileUri(item: DownloadModel): Uri? {
        val ctx = context ?: return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                Uri.parse(item.localPath)
            } else {

                val file = File(item.localPath)
                if (!file.exists()) return null
                FileProvider.getUriForFile(
                    ctx,
                    "${ctx.packageName}.fileprovider",
                    file
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun openDownloadFolder(item: DownloadModel) {
        val folderName = STORAGE_FOLDER

        try {
            val folderUri = Uri.parse(
                "content://com.android.externalstorage.documents/document/primary:" +
                        Uri.encode(if (item.isAudio) "Music/$folderName" else "Movies/$folderName")
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(folderUri, "vnd.android.document/directory")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.parse(
                        "content://com.android.externalstorage.documents/tree/primary:" +
                                Uri.encode(if (item.isAudio) "Music/$folderName" else "Movies/$folderName")
                    ),
                    "vnd.android.document/directory"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            val fileUri = getFileUri(item) ?: return
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, if (item.isAudio) "audio/*" else "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }


        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}