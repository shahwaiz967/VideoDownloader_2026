package com.codecx.statussaver.features.composes.mainBoarding.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.codecx.statussaver.core.ui.states.ResultData
import com.codecx.statussaver.core.utils.exceptions.PathRequired
import com.codecx.statussaver.core.utils.exceptions.PermissionRequired
import com.codecx.statussaver.data.models.StatusItem

class PreviewProvider : PreviewParameterProvider<ResultData<List<StatusItem>>> {
    override val values: Sequence<ResultData<List<StatusItem>>>
        get() = sequenceOf(
            ResultData.Init,
            ResultData.Loading,
            ResultData.Success(
                listOf(
                    StatusItem(path = "1", isSaved = false, mimeType = "image/*"),
                    StatusItem(path = "2", isSaved = false, mimeType = "image/*"),
                    StatusItem(path = "5", isSaved = false, mimeType = "image/*"),
                    StatusItem(path = "3", isSaved = true, mimeType = "video/*"),
                    StatusItem(path = "4", isSaved = true, mimeType = "video/*"),
                    StatusItem(path = "6", isSaved = true, mimeType = "video/*")
                )
            ),
            ResultData.Error(exception = PermissionRequired()),
            ResultData.Error(exception = PathRequired())
        )
}