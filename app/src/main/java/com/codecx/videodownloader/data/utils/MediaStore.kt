package com.codecx.videodownloader.data.utils

import android.content.Context
import android.provider.MediaStore
import com.codecx.videodownloader.data.models.MediaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaStoreHelper {


    suspend fun getLocalVideos(context: Context, folder: String = "All"): List<MediaModel> {
        return withContext(Dispatchers.IO) {
            val videos = mutableListOf<MediaModel>()
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )


            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val bucketName = cursor.getString(bucketCol) ?: "Unknown"
                    if (folder == "All" || bucketName == folder) {
                        videos.add(
                            MediaModel(
                                id = cursor.getLong(idCol),
                                title = cursor.getString(nameCol) ?: "",
                                path = cursor.getString(dataCol) ?: "",
                                duration = cursor.getLong(durCol),
                                size = cursor.getLong(sizeCol),
                                dateAdded = cursor.getLong(dateCol),
                                platform = bucketName,
                                isAudio = false
                            )
                        )
                    }
                }
            }
            videos
        }
    }


    suspend fun getLocalAudio(context: Context, folder: String = "All"): List<MediaModel> {
        return withContext(Dispatchers.IO) {
            val audios = mutableListOf<MediaModel>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            )

            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val bucketName = cursor.getString(bucketCol) ?: "Unknown"
                    if (folder == "All" || bucketName == folder) {
                        audios.add(
                            MediaModel(
                                id = cursor.getLong(idCol),
                                title = cursor.getString(nameCol) ?: "",
                                path = cursor.getString(dataCol) ?: "",
                                duration = cursor.getLong(durCol),
                                size = cursor.getLong(sizeCol),
                                dateAdded = cursor.getLong(dateCol),
                                platform = bucketName,
                                isAudio = true
                            )
                        )
                    }
                }
            }
            audios
        }
    }


    suspend fun getVideoFolders(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            val folders = linkedSetOf<String>()
            val projection = arrayOf(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val bucket = cursor.getString(bucketCol)
                    if (!bucket.isNullOrEmpty()) folders.add(bucket)
                }
            }
            folders.toList()
        }
    }


    suspend fun getAudioFolders(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            val folders = linkedSetOf<String>()
            val projection = arrayOf(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val bucket = cursor.getString(bucketCol)
                    if (!bucket.isNullOrEmpty()) folders.add(bucket)
                }
            }
            folders.toList()
        }
    }
}