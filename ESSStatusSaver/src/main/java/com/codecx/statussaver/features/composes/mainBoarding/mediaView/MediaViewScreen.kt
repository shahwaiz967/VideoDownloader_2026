package com.codecx.statussaver.features.composes.mainBoarding.mediaView

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.codecx.statussaver.R
import com.codecx.statussaver.core.navigation.LocalNavController
import com.codecx.statussaver.core.navigation.Route
import com.codecx.statussaver.core.ui.states.ResultData
import com.codecx.statussaver.core.ui.theme.StatusSaverTheme
import com.codecx.statussaver.data.models.StatusItem
import com.codecx.statussaver.features.composes.mainBoarding.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel


@Immutable
data class BottomItem(val id: Int, val icon: Int, val title: String)

@Composable
fun MediaViewScreen(navEntry: NavBackStackEntry) {
    val args = navEntry.toRoute<Route.MediaView>()
    val navController = LocalNavController.current
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Route.Home)
    }
    val vm = koinViewModel<HomeViewModel>(viewModelStoreOwner = parentEntry)
    val dataState by vm.statuses.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val filteredData by remember(args, dataState) {
        derivedStateOf {
            if (dataState is ResultData.Success) {
                val data = (dataState as ResultData.Success<List<StatusItem>>).data
                when (args.tabId) {
                    1 -> data.filter { !it.isVideo && !it.isUserSaved }
                    2 -> data.filter { it.isVideo && !it.isUserSaved }
                    3 -> data.filter { it.isUserSaved }
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        }
    }
    val currentPosition by remember(filteredData, args) {
        derivedStateOf {
            val index = filteredData.indexOfFirst { it.path == args.selectedPath }
            if (index >= 0) index else 0
        }
    }
    MediaViewContent(filteredData, currentPosition, onBottomActionClick = { item, id ->
        when (id) {
            0 -> {
                //Repost
                item.getShareIntent()?.let { intent ->
                    context.startActivity(intent)
                }
            }

            1 -> {
                //Share
                item.getShareIntent()?.let { intent ->
                    context.startActivity(intent)
                }
            }

            2 -> {
                //Save
                if (!item.isSaved)
                    vm.saveStatus(item, onSuccess = {
                        Toast.makeText(context,
                            R.string.saved, Toast.LENGTH_SHORT).show()
                    }, onError = {
                        Toast.makeText(context,
                            R.string.fail_to_save, Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }, onNavUp = {
        navController.popBackStack()
    }, onVideoPlay = { item ->
        /*item.getPlayIntent()?.let { intent ->
            context.startActivity(intent)
        }*/
        val encodedPath = Uri.encode(item.path)
        val encodedTitle = Uri.encode(item.title)
        val deepLinkUri = "app://videodownloader/play?videoPath=$encodedPath&videoTitle=$encodedTitle&isAudio=false".toUri()

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri)
        intent.setPackage(context.packageName)
        context.startActivity(intent)

    }, tabId = args.tabId)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaViewContent(
    filteredData: List<StatusItem> = emptyList(),
    currentPosition: Int = 0,
    tabId: Int = 1,
    onNavUp: () -> Unit = {},
    onBottomActionClick: (StatusItem, Int) -> Unit = { _, _ -> },
    onVideoPlay: (StatusItem) -> Unit = {}
) {
    val buttonButtons = if (tabId == 3) {
        listOf(
            BottomItem(0, R.drawable.ic_share, stringResource(R.string.repost)),
            BottomItem(1, R.drawable.ic_share_v, stringResource(R.string.share))
        )
    } else {
        listOf(
            BottomItem(0, R.drawable.ic_share, stringResource(R.string.repost)),
            BottomItem(1, R.drawable.ic_share_v, stringResource(R.string.share)),
            BottomItem(2, R.drawable.icon_download_file, stringResource(R.string.save))
        )
    }
    val pagerState = rememberPagerState(initialPage = currentPosition) {
        filteredData.size
    }
    val currentTitle by remember(pagerState) {
        derivedStateOf {
            if (filteredData.lastIndex >= pagerState.currentPage) {
                filteredData[pagerState.currentPage].title
            } else {
                ""
            }
        }
    }
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            add(VideoFrameDecoder.Factory())
        }.crossfade(true).diskCache {
            DiskCache.Builder().maxSizePercent(0.02).build()
        }.memoryCache {
            MemoryCache.Builder(context).maxSizePercent(0.25).build()
        }.build()
    }
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .systemBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium, maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavUp) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = LocalContentColor.current,
                    titleContentColor = LocalContentColor.current
                )
            )
            HorizontalPager(
                pagerState, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { index ->
                val page = filteredData[index]
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(page.path)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    if (page.isVideo) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color = Color.Black.copy(0.3f))
                                .clickable(onClick = {
                                    onVideoPlay.invoke(page)
                                })
                                .padding(7.dp),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                buttonButtons.forEach { button ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                            .clickable(onClick = {
                                onBottomActionClick(filteredData[pagerState.currentPage], button.id)
                            })
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(button.icon),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                        Text(
                            button.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun MediaViewPreview() {
    StatusSaverTheme {
        MediaViewContent()
    }
}