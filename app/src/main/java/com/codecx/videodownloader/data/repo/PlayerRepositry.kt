package com.codecx.videodownloader.data.repo

import android.content.Context
import com.codecx.videodownloader.data.models.MediaModel
import com.codecx.videodownloader.data.utils.MediaStoreHelper

class PlayerRepositry {
    suspend fun getVideos(
        context: Context,
        folder: String = "All"
    ): List<
            MediaModel> {
        return MediaStoreHelper.getLocalVideos(context, folder)
    }

    suspend fun getAudios(
        context: Context,
        folder: String = "All"
    ): List<MediaModel> {
        return MediaStoreHelper.getLocalAudio(context, folder)
    }

    suspend fun getVideoFolders(
        context: Context
    ): List<String> {
        return MediaStoreHelper.getVideoFolders(context)
    }

    suspend fun getAudioFolders(
        context: Context
    ): List<String> {
        return MediaStoreHelper.getAudioFolders(context)
    }
}