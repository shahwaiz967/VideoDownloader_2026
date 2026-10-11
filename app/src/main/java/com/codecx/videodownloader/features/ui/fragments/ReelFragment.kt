package com.codecx.videodownloader.features.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.models.ReelItem
import com.codecx.videodownloader.data.states.ResultState
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.databinding.FragmentReelBinding
import com.codecx.videodownloader.features.adapters.ReelsPagerAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

@androidx.media3.common.util.UnstableApi
class ReelFragment : BaseFragment<FragmentReelBinding>() {

    private lateinit var reelsAdapter: ReelsPagerAdapter
    private var reelId = 0
    private val downloadViewModel: DownloadViewModel by activityViewModel()

    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentReelBinding {
        return FragmentReelBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (parentFragment is MainFragment) {
            binding.ivBack.visibility = View.GONE
        } else {
            binding.ivBack.visibility = View.VISIBLE
            binding.ivBack.setOnClickListener {
                safePopBackStack()
            }
        }
        reelId = arguments?.getInt(EXTRA_CATEGORY_ID) ?: 0
        goFullscreen()
        fetchReels()
        observeDownloadCompletion()

        bindNative(
            RemoteConfigHelper.adIds.reelBottomNative,
            false,
            binding.adContainer
        )
    }

    override fun onDetach() {
        super.onDetach()
        val window = activity?.window ?: return
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.show(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun goFullscreen() {
        val window = activity?.window ?: return
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun fetchReels() {
        showLoading(true)
        lifecycleScope.launch {
            downloadViewModel.trendingVideos.collectLatest {
                when (it) {
                    is ResultState.Init -> {}
                    is ResultState.Loading -> {
                        showLoading(true)
                    }

                    is ResultState.Success -> {
                        showLoading(false)
                        val videos = it.data.data ?: emptyList()
                        val videoItems = videos.mapNotNull { video ->
                            val url = video.url ?: return@mapNotNull null
                            ReelItem.VideoItem(
                                id = video.id ?: 0,
                                url = url,
                                name = video.name ?: "",
                                thumbnail = video.thumbnail ?: "",
                                category = video.category ?: "",
                                likes = video.likes ?: 0,
                                downloads = video.downloads ?: 0,
                                videoClass = video.videoClass ?: "",
                                createdAt = video.createdAt ?: "",
                                updatedAt = video.updatedAt ?: ""
                            )
                        }
                        if (videoItems.isEmpty()) {
                            showError(getString(R.string.no_videos_found))
                        } else {
                            val items = buildReelListWithAds(videoItems)
                            setupViewPager(items)
                        }
                    }

                    is ResultState.Error -> {
                        showLoading(false)
                        showError(getString(R.string.network_error, it.message ?: ""))
                    }
                }

            }


        }
    }

    private fun buildReelListWithAds(videoItems: List<ReelItem.VideoItem>): List<ReelItem> {
        val adConfig = RemoteConfigHelper.adIds.reelFullNative
        val adController = ReelsPagerAdapter.getAdControllerValue(adConfig)
        return ReelsPagerAdapter.buildReelListWithAds(videoItems, adConfig, adController)
    }

    private fun setupViewPager(items: List<ReelItem>) {
        reelsAdapter = ReelsPagerAdapter(requireContext(), items) { reel ->
            val downloadItem = DownloadModel(
                title = reel.name.ifEmpty { getString(R.string.reel_video) },
                url = reel.url,
                thumbnailUrl = reel.thumbnail,
                fileSize = "",
                isAudio = false
            )
            downloadViewModel.startDownload(downloadItem)
            Toast.makeText(requireContext(), getString(R.string.download_started), Toast.LENGTH_SHORT).show()
        }

        binding.viewPager.apply {
            adapter = reelsAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    reelsAdapter.currentPlayingPosition = position
                    updateBottomNativeAdVisibility(position)
                }
            })

            post {
                reelsAdapter.currentPlayingPosition = reelId
                if (reelId != 0) setCurrentItem(reelId, false)
                updateBottomNativeAdVisibility(reelId)
            }
        }
    }

    private fun updateBottomNativeAdVisibility(position: Int) {
        if (!::reelsAdapter.isInitialized) return
        val isAdItem = reelsAdapter.isAdPosition(position)
        val isBottomAdEnabled = RemoteConfigHelper.adIds.reelBottomNative.isAdEnable
        if (isAdItem || !isBottomAdEnabled) {
            binding.adContainer.visibility = View.GONE
        } else {
            binding.adContainer.visibility = View.VISIBLE
        }
    }

    private fun observeDownloadCompletion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadViewModel.downloadCompleteEvent.collectLatest {
                    Toast.makeText(requireContext(), getString(R.string.download_complete), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        Log.d("VideoError", "showError: $msg ")
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        if (::reelsAdapter.isInitialized) reelsAdapter.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        if (::reelsAdapter.isInitialized) reelsAdapter.resumeCurrent()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::reelsAdapter.isInitialized) reelsAdapter.releaseAll()
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "reel_id"
    }
}
