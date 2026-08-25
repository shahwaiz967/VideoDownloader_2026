package com.codecx.statussaver.features.composes.mainBoarding.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.size.Scale
import com.codecx.statussaver.R
import com.codecx.statussaver.core.navigation.LocalNavController
import com.codecx.statussaver.core.navigation.Route
import com.codecx.statussaver.core.ui.states.ResultData
import com.codecx.statussaver.core.ui.theme.Paddings
import com.codecx.statussaver.core.ui.theme.StatusSaverTheme
import com.codecx.statussaver.core.utils.PathUtils
import com.codecx.statussaver.core.utils.exceptions.PathRequired
import com.codecx.statussaver.core.utils.exceptions.PermissionRequired
import com.codecx.statussaver.data.models.StatusItem
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.absoluteValue

@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val vm = koinViewModel<HomeViewModel>()
    val dataState by vm.statuses.collectAsStateWithLifecycle()
    HomeScreenContent(onTopActionClick = { id ->
        when (id) {
            0 -> navController.navigate(Route.Settings)
            1 -> navController.navigate(Route.Premium)
        }
    }, dataState = dataState, refreshData = {
        vm.loadData()
    }, onPathSelected = { uri ->
        vm.updateSelectedDirPath(uri.toString())
    }, onItemClick = { item, tabId ->
        navController.navigate(Route.MediaView(selectedPath = item.path, tabId = tabId))
    })
}

@Immutable
data class TabItem(val id: Int, val title: String, val icon: Int)

@Composable
private fun HomeScreenContent(
    dataState: ResultData<List<StatusItem>>,
    refreshData: () -> Unit = {},
    onPathSelected: (Uri) -> Unit = {},
    onTopActionClick: (Int) -> Unit = {},
    onItemClick: (StatusItem, Int) -> Unit = { _, _ -> }
) {
    val activity = LocalActivity.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.all { m -> m }) {
                refreshData()
            }
        }
    val permissionIntentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshData()
        }
    val pathLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            if (PathUtils.isRightDirectory(it)) {
                activity?.contentResolver?.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onPathSelected.invoke(it)
            } else {
                Toast.makeText(
                    activity,
                    R.string.please_select_the_statuses_folder_specifically,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    val tabs = listOf(
        TabItem(1, stringResource(R.string.images), R.drawable.ic_image),
        TabItem(2, stringResource(R.string.videos), R.drawable.ic_video),
        TabItem(3, stringResource(R.string.saved), R.drawable.icon_download_file)
    )
    val pagerState = rememberPagerState {
        tabs.size
    }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.save_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))


                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabs.forEach { tab ->
                        key(tab.id) {
                            val pageOffset =
                                pagerState.getOffsetDistanceInPages(tab.id - 1).absoluteValue.coerceIn(
                                    0f, 1f
                                )
                            val textColor = androidx.compose.ui.graphics.lerp(
                                MaterialTheme.colorScheme.onPrimary.copy(0.5f),
                                MaterialTheme.colorScheme.onPrimary,
                                1f - pageOffset
                            )
                            val indicatorAlpha = lerp(
                                0f, 1f, 1f - pageOffset
                            )
                            ConstraintLayout(modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(tab.id - 1)
                                }
                            }) {
                                val (textRef, indicatorRef) = createRefs()
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.constrainAs(textRef) {
                                        start.linkTo(parent.start)
                                        end.linkTo(parent.end)
                                        top.linkTo(parent.top)
                                    },
                                    color = textColor
                                )
                                Spacer(
                                    modifier = Modifier
                                        .constrainAs(indicatorRef) {
                                            start.linkTo(parent.start)
                                            end.linkTo(parent.end)
                                            top.linkTo(textRef.bottom)
                                            width = Dimension.fillToConstraints
                                        }
                                        .padding(top = 3.dp)
                                        .height(4.dp)
                                        .alpha(indicatorAlpha)
                                        .clip(
                                            RoundedCornerShape(100)
                                        )
                                        .background(MaterialTheme.colorScheme.onPrimary))

                            }
                        }
                    }
                }
            }
        }
    }) { innerPadding ->
        HorizontalPager(
            pagerState, modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
        ) { page ->
            SlidePage(
                pageItem = tabs[page], dataState = dataState, onActionButtonClick = { exception ->
                    when (exception) {
                        is PathRequired -> {
                            exception.requestForPath(pathLauncher = pathLauncher)
                        }

                        is PermissionRequired -> {
                            activity?.let {
                                exception.requestForPermission(
                                    activity = it,
                                    activityResultLauncher = permissionIntentLauncher,
                                    permissionResultLauncher = permissionLauncher
                                )
                            }
                        }
                    }
                }, onItemClick = {
                    onItemClick.invoke(it, tabs[page].id)
                })
        }
    }
}

@Composable
private fun SlidePage(
    pageItem: TabItem,
    dataState: ResultData<List<StatusItem>>,
    onItemClick: (StatusItem) -> Unit = {},
    onActionButtonClick: (Throwable) -> Unit
) {
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
    val filteredData by remember(dataState) {
        derivedStateOf {
            if (dataState is ResultData.Success) {
                val data = dataState.data
                when (pageItem.id) {
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
    AnimatedContent(dataState, modifier = Modifier.fillMaxSize()) { state ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
                is ResultData.Loading -> CircularProgressIndicator()
                is ResultData.Init -> Text(stringResource(R.string.loading))
                is ResultData.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Image(
                            painter = painterResource(pageItem.icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFB8D2D2),
                                            Color(0xFFBDD5D5),
                                            Color(0xFFCEE0E0),
                                            Color(0xFFE9F1F1),
                                            Color(0xFFFFFFFF),
                                        )
                                    )
                                )
                                .padding(50.dp)
                        )
                        val actionLabel = if (state.exception is PermissionRequired) {
                            stringResource(R.string.allow_permission)
                        } else {
                            stringResource(R.string.select_path)
                        }
                        Button(onClick = {
                            onActionButtonClick.invoke(state.exception)
                        }, modifier = Modifier.fillMaxWidth(0.6f)) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                is ResultData.Success -> {
                    if (filteredData.isEmpty()) {
                        Text(stringResource(R.string.no_data_found))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(
                                Paddings.SafePadding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = filteredData, key = StatusItem::path) { item ->
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                ) {
                                    val boxWidth = this.maxWidth
                                    val newHeight = (boxWidth.value * 1.1f).dp
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.path)
                                            .crossfade(true)
                                            .scale(Scale.FILL)
                                            .build(),
                                        imageLoader = imageLoader,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(newHeight)
                                            .dropShadow(
                                                shape = RoundedCornerShape(10.dp),
                                                shadow = Shadow(
                                                    3.dp,
                                                    color = Color.Black.copy(0.3f)
                                                )
                                            )
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(onClick = {
                                                onItemClick.invoke(item)

                                            }),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                    if (item.isVideo) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(color = Color.Black.copy(0.3f))
                                                .padding(7.dp),
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_9_PRO)
private fun HomeScreenContentPreview(@PreviewParameter(PreviewProvider::class) provider: ResultData<List<StatusItem>>) {
    StatusSaverTheme {
        HomeScreenContent(dataState = provider)
    }
}