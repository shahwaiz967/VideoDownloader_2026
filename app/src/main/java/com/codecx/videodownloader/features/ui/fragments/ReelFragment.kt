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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.codecx.videodownloader.data.models.DownloadModel
import com.codecx.videodownloader.data.models.ReelItem
import com.codecx.videodownloader.data.states.ResultState
import com.codecx.videodownloader.databinding.FragmentReelBinding
import com.codecx.videodownloader.features.adapters.ReelsPagerAdapter
import com.codecx.videodownloader.features.base.BaseFragment
import com.codecx.videodownloader.features.ui.viewmodel.DownloadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

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

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        reelId = arguments?.getInt(EXTRA_CATEGORY_ID) ?: 0
        goFullscreen()
        fetchReels()
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
                        val items = videos.mapNotNull { video ->
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
                        if (items.isEmpty()) {
                            showError("No videos found")
                        } else {
                            setupViewPager(items)
                        }
                    }

                    is ResultState.Error -> {
                        showLoading(false)
                        showError("Network error: ${it.message}")
                    }
                }

            }


        }
    }

    private fun setupViewPager(items: List<ReelItem>) {
        reelsAdapter = ReelsPagerAdapter(requireContext(), items) { reel ->
            val downloadItem = DownloadModel(
                title = reel.name.ifEmpty { "Reel Video" },
                url = reel.url,
                thumbnailUrl = reel.thumbnail,
                fileSize = "",
                isAudio = false
            )
            downloadViewModel.startDownload(downloadItem)
            Toast.makeText(requireContext(), "Download Started...", Toast.LENGTH_SHORT).show()
        }

        binding.viewPager.apply {
            adapter = reelsAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    reelsAdapter.currentPlayingPosition = position
                }
            })

            post {
                reelsAdapter.currentPlayingPosition = reelId
                if (reelId != 0) setCurrentItem(reelId, false)
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