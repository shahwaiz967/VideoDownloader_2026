package com.codecx.videodownloader.features.ui.fragments

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.fragment.findNavController
import com.codecx.videodownloader.R
import com.codecx.videodownloader.data.ads.utils.bindNative
import com.codecx.videodownloader.data.utils.RemoteConfigHelper
import com.codecx.videodownloader.data.utils.safePopBackStack
import com.codecx.videodownloader.features.ui.viewmodel.VideoPlayerViewModel
import com.codecx.videodownloader.databinding.FragmentMediaPlayBinding
import com.codecx.videodownloader.features.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaPlayFragment : BaseFragment<FragmentMediaPlayBinding>() {

    private val viewModel: VideoPlayerViewModel by viewModel()

    private var videoPath = ""
    private var videoTitle = ""
    private var isAudio = false

    private var isFullscreen = false

    private var playerViewOriginalParams: ConstraintLayout.LayoutParams? = null


    override fun bindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMediaPlayBinding {
        return FragmentMediaPlayBinding.inflate(
            inflater,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->

            videoPath = viewModel.getMediaPath(
                bundle.getString("videoPath", "")
            )

            videoTitle = viewModel.getMediaTitle(
                bundle.getString("videoTitle", "")
            )

            isAudio = viewModel.isAudio(
                bundle.getBoolean("isAudio", false)
            )
        }
        bindNative(
            RemoteConfigHelper.adIds.videoPlayerNative,
            false,
            binding.adLayoutBottom
        )

        setupUi()
        setupListeners()
        setupPlayer()
    }

    /*private fun expandPlayerViewToFillRoot() {
        val lp = binding.playerView.layoutParams as? ConstraintLayout.LayoutParams ?: return

        if (playerViewOriginalParams == null) {
            playerViewOriginalParams = ConstraintLayout.LayoutParams(lp)
        }

        lp.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        lp.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        lp.topToBottom = ConstraintLayout.LayoutParams.UNSET
        lp.bottomToTop = ConstraintLayout.LayoutParams.UNSET
        lp.setMargins(0, 0, 0, 0)

        binding.playerView.layoutParams = lp
        binding.playerView.requestLayout()
    }

    private fun restorePlayerViewLayout() {
        val original = playerViewOriginalParams ?: return
        binding.playerView.layoutParams = ConstraintLayout.LayoutParams(original)
        binding.playerView.requestLayout()
    }*/

    private fun setupUi() {

        binding.tvTitle.text = videoTitle

        binding.layoutAudioPlayer.isVisible = isAudio

        binding.playerView.isVisible = !isAudio
    }

    private fun setupPlayer() {
        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        binding.playerView.player = viewModel.player

        viewModel.player.addListener(playerListener)

        if (videoPath.isNotEmpty()) {
            viewModel.playMedia(videoPath)
        }
    }

    private fun setupListeners() {

        binding.ivBack.setOnClickListener {
            safePopBackStack()
        }

        binding.ivPlayPause.setOnClickListener {

            viewModel.togglePlayPause()
        }

        binding.ivSkipBack.setOnClickListener {

            viewModel.seekBack()
        }

        binding.ivSkipForward.setOnClickListener {

            viewModel.seekForward()
        }

        binding.ivRotate.setOnClickListener {

            requireActivity().requestedOrientation =
                if (
                    resources.configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
                ) {

                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                } else {

                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                }
        }

        binding.ivFullscreen.setOnClickListener {

            toggleFullscreen()
        }

        binding.playerView.setOnClickListener {

            val visible = binding.toolbar.isVisible

            binding.toolbar.isVisible = !visible
            binding.layoutControls.isVisible = !visible
        }
    }

    private val playerListener =
        object : Player.Listener {

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {

                when (playbackState) {

                    Player.STATE_BUFFERING -> {
                        binding.progressBuffering.isVisible = true
                    }

                    Player.STATE_READY -> {
                        binding.progressBuffering.isVisible = false
                    }

                    Player.STATE_ENDED -> {
                        binding.progressBuffering.isVisible = false
                        binding.ivPlayPause.setImageResource(
                            R.drawable.ic_play
                        )
                    }

                    Player.STATE_IDLE -> {
                        binding.progressBuffering.isVisible = false
                    }
                }
            }

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {

                binding.ivPlayPause.setImageResource(
                    if (isPlaying) {
                        R.drawable.ic_pause
                    } else {
                        R.drawable.ic_play
                    }
                )
            }
        }

    private fun toggleFullscreen() {

        isFullscreen = !isFullscreen

        if (isFullscreen) {

            requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            binding.toolbar.isVisible = false
            //expandPlayerViewToFillRoot()

        } else {
            requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.toolbar.isVisible = true
            //restorePlayerViewLayout()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.playerView.player = viewModel.player
    }

    override fun onStop() {
        binding.playerView.player = null
        viewModel.player.removeListener(playerListener)

        super.onStop()
    }

    override fun onDestroyView() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onDestroyView()
    }

}