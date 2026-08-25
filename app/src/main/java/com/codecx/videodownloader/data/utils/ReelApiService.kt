package com.codecx.videodownloader.data.utils

import android.util.Log
import com.codecx.videodownloader.data.models.TrendingVideoResponse
import com.codecx.videodownloader.data.states.ResultState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ReelApiService(private val client: HttpClient) {

    fun getTrendingVideos(limit: Int = 100) = flow {
        runCatching {
            val fullUrl =
                "${AppConstants.REEL_BASE_URL}videodownloader/api/videos/gettrendingvideos/$limit"

            val response = client.get(fullUrl)

            if (response.status.isSuccess()) {
                response.body<TrendingVideoResponse>()
            } else {
                val errorBody = response.bodyAsText()
                Log.e("ReelApi", "Server Error (${response.status.value}): $errorBody")
                throw Exception("HTTP ${response.status.value}: $errorBody")
            }
        }.onSuccess {
            emit(ResultState.Success(it))
        }.onFailure { error ->
            emit(ResultState.Error(error.message ?: "Something went wrong"))
        }

    }.flowOn(Dispatchers.IO)
}
