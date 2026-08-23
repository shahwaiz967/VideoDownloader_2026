package com.codecx.videodownloader.features.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.codecx.videodownloader.data.repo.VideoPlayerRepo

class VideoPlayerViewModel(
    application: Application,
    private val videoPlayerRepo: VideoPlayerRepo
) : AndroidViewModel(application) {

    val player: ExoPlayer =
        ExoPlayer.Builder(application).build()

    private var currentPath: String? = null

    fun playMedia(path: String) {

        val mediaPath = videoPlayerRepo.getMediaPath(path)

        if (currentPath == mediaPath) {
            return
        }

        currentPath = mediaPath

        val mediaItem = MediaItem.fromUri(mediaPath)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekBack() {
        player.seekBack()
    }

    fun seekForward() {
        player.seekForward()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun getMediaTitle(title: String): String {
        return videoPlayerRepo.getMediaTitle(title)
    }

    fun isAudio(isAudio: Boolean): Boolean {
        return videoPlayerRepo.isAudio(isAudio)
    }
    fun getMediaPath(path : String) : String
    {
        return videoPlayerRepo.getMediaPath(path)
    }
    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}