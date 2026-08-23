package com.codecx.videodownloader.data.utils

import android.util.Log
import com.codecx.videodownloader.data.models.TrendingVideoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class ReelApiService(private val client: HttpClient) {

    suspend fun getTrendingVideos(limit: Int = 100): Result<TrendingVideoResponse> {
        return runCatching {
            val fullUrl = "${AppConstants.REEL_BASE_URL}videodownloader/api/videos/gettrendingvideos/$limit"

            val response = client.get(fullUrl)

            if (response.status.isSuccess()) {
                response.body<TrendingVideoResponse>()
            } else {
                val errorBody = response.bodyAsText()
                Log.e("ReelApi", "Server Error (${response.status.value}): $errorBody")
                throw Exception("HTTP ${response.status.value}: $errorBody")
            }
        }
    }
}
