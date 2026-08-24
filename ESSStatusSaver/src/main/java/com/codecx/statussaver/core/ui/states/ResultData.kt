package com.codecx.statussaver.core.ui.states

sealed class ResultData<out T> {
    data object Init : ResultData<Nothing>()
    data object Loading : ResultData<Nothing>()
    data class Success<T>(val data: T) : ResultData<T>()
    data class Error(val exception: Throwable) : ResultData<Nothing>()
}