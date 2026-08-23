package com.codecx.videodownloader.features.ui.fragments

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.models.TabModel
import com.codecx.videodownloader.data.utils.AppConstants
import com.codecx.videodownloader.data.utils.safeNavigate
import com.codecx.videodownloader.features.viewmodel.TabViewModel
import com.codecx.videodownloader.databinding.FragmentMainBinding
import com.codecx.videodownloader.features.adapters.TabAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.viewmodel.DownloadViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class MainFragment : BaseFragment<FragmentMainBinding>(){

    private val tabViewModel: TabViewModel by viewModel()
    private val downloadViewModel : DownloadViewModel by activityViewModel()
    private val STORAGE_FOLDER = "VideoDownloader"

    private lateinit var ivThumbnail : ImageView

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null){
            loadFragment(HomeFragment())
        }

        binding.tabCard.setOnClickListener {
            tabBottomSheet()
        }
       navigation()

        observeDownloadCompletion()
    }

    private fun observeDownloadCompletion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadViewModel.downloadCompleteEvent.collectLatest { downloadItem ->
                    showDownloadCompleteSheet(downloadItem)
                }
            }
        }
    }



    private fun navigation(){
        binding.navHome.setOnClickListener {
            loadFragment(HomeFragment())
        }
        binding.navDownloads.setOnClickListener {
            loadFragment(DownloadFragment())
        }
        binding.navPlayer.setOnClickListener {
            loadFragment(PlayerFragment())
        }
        binding.navReels.setOnClickListener {
            safeNavigate(R.id.reelFragment)
        }
        binding.btnReels.setOnClickListener {
            safeNavigate(R.id.reelFragment)
        }
        binding.ivPremium.setOnClickListener {
            safeNavigate(R.id.premiumFragment)
        }
        binding.ivSettings.setOnClickListener {
            safeNavigate(R.id.settingFragment)
        }
    }


    private fun loadFragment(fragment: Fragment){
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,fragment)
            .commit()
    }

    private fun tabBottomSheet() {

        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheet.setContentView(R.layout.tab_bottom_sheet)

        val backArrow = bottomSheet.findViewById<ImageView>(R.id.backButton)
        val btnAddTab = bottomSheet.findViewById<MaterialButton>(R.id.btnAddTab)
        val btnClearAll = bottomSheet.findViewById<TextView>(R.id.btnClearAll)
        val tvTabCount = bottomSheet.findViewById<TextView>(R.id.tvTabCount)
        val rvTabs = bottomSheet.findViewById<RecyclerView>(R.id.rvTabs)

        val adapter = TabAdapter(
            onTabClick = { tab ->
                tabViewModel.selectTab(tab.id)
                bottomSheet.dismiss()
                openTab(tab)
            },
            onCloseClick = { tab ->
                tabViewModel.removeTab(tab.id)
            }
        )

        rvTabs?.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            this.adapter = adapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                tabViewModel.tabs.collect { tabs ->
                    adapter.submitList(tabs)
                    tvTabCount?.text = "Tabs (${tabs.size})"
                    binding.ivTabBadge.text = tabs.size.toString()
                }
            }
        }

        backArrow?.setOnClickListener {
            bottomSheet.dismiss()
        }

        btnAddTab?.setOnClickListener {
            val newTab = tabViewModel.addTab()
            bottomSheet.dismiss()
            openTab(newTab)
        }

        btnClearAll?.setOnClickListener {
            tabViewModel.clearAll()
            bottomSheet.dismiss()
            val newTab = tabViewModel.addTab()
            openTab(newTab)
        }
        bottomSheet.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )
        bottomSheet.show()
    }

    private fun showDownloadCompleteSheet(item: DownloadModel) {
        val bottomSheet = BottomSheetDialog(requireContext())
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
        tvAuthor?.text = "Just now · ${item.fileSize}"


        if (item.thumbnailUrl.isNotEmpty()){
            Glide.with(requireContext())
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
                startActivity(Intent.createChooser(shareIntent, "Share via"))
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
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                Uri.parse(item.localPath)
            } else {

                val file = File(item.localPath)
                if (!file.exists()) return null
                FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
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

    private fun openTab(tab: TabModel) {

        loadFragment(
            HomeFragment.newInstance(
                tabId = tab.id
            )
        )
    }

}