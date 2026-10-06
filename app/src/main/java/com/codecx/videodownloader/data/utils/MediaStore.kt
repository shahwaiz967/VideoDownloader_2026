package com.codecx.videodownloader.data.utils

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.codecx.videodownloader.data.models.MediaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MediaStoreHelper {

    suspend fun getLocalVideos(context: Context, folder: String = "All"): List<MediaModel> {
        return withContext(Dispatchers.IO) {
            val videos = mutableListOf<MediaModel>()
            val hasBucketCol = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val projection = if (hasBucketCol) {
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                )
            } else {
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED
                )
            }

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            try {
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                    val nameCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                    val durCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                    val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                        val bucketName = if (bucketCol != -1) {
                            cursor.getString(bucketCol)
                        } else null
                        val folderName = bucketName?.ifEmpty { null }
                            ?: try { File(path).parentFile?.name } catch (e: Exception) { null }
                            ?: "Unknown"

                        if (folder == "All" || folderName == folder) {
                            videos.add(
                                MediaModel(
                                    id = if (idCol != -1) cursor.getLong(idCol) else 0L,
                                    title = if (nameCol != -1) cursor.getString(nameCol) ?: "" else "",
                                    path = path,
                                    duration = if (durCol != -1) cursor.getLong(durCol) else 0L,
                                    size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L,
                                    dateAdded = if (dateCol != -1) cursor.getLong(dateCol) else 0L,
                                    platform = folderName,
                                    isAudio = false
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videos
        }
    }

    suspend fun getLocalAudio(context: Context, folder: String = "All"): List<MediaModel> {
        return withContext(Dispatchers.IO) {
            val audios = mutableListOf<MediaModel>()
            val hasBucketCol = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val projection = if (hasBucketCol) {
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
                )
            } else {
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATE_ADDED
                )
            }

            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            try {
                context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val durCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                    val bucketCol = cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                        val bucketName = if (bucketCol != -1) {
                            cursor.getString(bucketCol)
                        } else null
                        val folderName = bucketName?.ifEmpty { null }
                            ?: try { File(path).parentFile?.name } catch (e: Exception) { null }
                            ?: "Unknown"

                        if (folder == "All" || folderName == folder) {
                            audios.add(
                                MediaModel(
                                    id = if (idCol != -1) cursor.getLong(idCol) else 0L,
                                    title = if (nameCol != -1) cursor.getString(nameCol) ?: "" else "",
                                    path = path,
                                    duration = if (durCol != -1) cursor.getLong(durCol) else 0L,
                                    size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L,
                                    dateAdded = if (dateCol != -1) cursor.getLong(dateCol) else 0L,
                                    platform = folderName,
                                    isAudio = true
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            audios
        }
    }

    suspend fun getVideoFolders(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            val folders = linkedSetOf<String>()
            val hasBucketCol = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val projection = if (hasBucketCol) {
                arrayOf(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA)
            } else {
                arrayOf(MediaStore.Video.Media.DATA)
            }

            try {
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, null
                )?.use { cursor ->
                    val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                    while (cursor.moveToNext()) {
                        val bucket = if (bucketCol != -1) cursor.getString(bucketCol) else null
                        val path = if (dataCol != -1) cursor.getString(dataCol) else null
                        val folderName = bucket?.ifEmpty { null }
                            ?: try { if (!path.isNullOrEmpty()) File(path).parentFile?.name else null } catch (e: Exception) { null }
                        if (!folderName.isNullOrEmpty()) folders.add(folderName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            folders.toList()
        }
    }

    suspend fun getAudioFolders(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            val folders = linkedSetOf<String>()
            val hasBucketCol = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val projection = if (hasBucketCol) {
                arrayOf(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME, MediaStore.Audio.Media.DATA)
            } else {
                arrayOf(MediaStore.Audio.Media.DATA)
            }

            try {
                context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, null
                )?.use { cursor ->
                    val bucketCol = cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    while (cursor.moveToNext()) {
                        val bucket = if (bucketCol != -1) cursor.getString(bucketCol) else null
                        val path = if (dataCol != -1) cursor.getString(dataCol) else null
                        val folderName = bucket?.ifEmpty { null }
                            ?: try { if (!path.isNullOrEmpty()) File(path).parentFile?.name else null } catch (e: Exception) { null }
                        if (!folderName.isNullOrEmpty()) folders.add(folderName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            folders.toList()
        }
    }
}
