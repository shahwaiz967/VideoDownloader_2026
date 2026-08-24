package com.codecx.statussaver.features.composes.mainBoarding.premium

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Devices.PIXEL_9_PRO
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codecx.statussaver.BuildConfig
import com.codecx.statussaver.R
import com.codecx.statussaver.core.ui.theme.StatusSaverTheme
import com.codecx.statussaver.core.utils.BillingHelperNew
import com.codecx.statussaver.data.models.PricesMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun PremiumScreen() {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val billingHelperNew = koinInject<BillingHelperNew>()
    val subs by billingHelperNew.offersUpdateListener.collectAsStateWithLifecycle()
    PremiumContent(
        onClose = {
            activity?.finish()
        },
        subs = subs, onRestore = {
            scope.launch {
                val restored = withContext(Dispatchers.IO) {
                    billingHelperNew.restorePurchases()
                }
                Toast.makeText(
                    context,
                    if (restored) "Restored" else "Purchase not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, onPurchase = { item ->
            if (activity != null) {
                billingHelperNew.launchBilling(item, activity)
            }
        }
    )
}

@Composable
private fun PremiumContent(
    onClose: () -> Unit = {},
    onPurchase: (PricesMenu) -> Unit = {},
    onRestore: () -> Unit = {},
    subs: List<PricesMenu> = emptyList()
) {
    var selectedItem by remember {
        mutableStateOf(BuildConfig.SUB_MONTHLY)
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            IconButton(
                onClick = onClose, modifier = Modifier
                    .align(
                        Alignment.End
                    )

            ) {
                Icon(Icons.Default.Close, contentDescription = "")
            }
            Image(
                painter = painterResource(R.drawable.ic_crown_ring),
                contentDescription = "",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(120.dp)
            )
            Text(
                "Upgrade to Premium",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                listOf(
                    BuildConfig.SUB_WEEKLY,
                    BuildConfig.SUB_MONTHLY,
                    BuildConfig.SUB_YEARLY
                ).forEach { pItem ->
                    key(pItem) {
                        val time = when (pItem) {
                            BuildConfig.SUB_MONTHLY -> stringResource(R.string.monthly)
                            BuildConfig.SUB_WEEKLY -> stringResource(R.string.weekly)
                            BuildConfig.SUB_YEARLY -> stringResource(R.string.yearly)
                            else -> ""
                        }
                        val item = subs.find {
                            it.productId == pItem
                        }
                        PremiumItem(
                            item = item,
                            isSelected = selectedItem == pItem,
                            time = time,
                            onClick = { selectedItem = pItem
                            })
                    }
                }
            }

            Text(
                "Benefits",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(vertical = 15.dp)
            ) {
                PinItem(stringResource(R.string.complete_access_to_all_widgets))
                PinItem(stringResource(R.string.future_updates))
                PinItem(stringResource(R.string.support_more_quality_widgets))
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    buildAnnotatedString {
                        withLink(LinkAnnotation.Url(url = stringResource(R.string.privacy_url))) {
                            append(stringResource(R.string.privacy_policy))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textDecoration = TextDecoration.Underline
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
                Text(
                    buildAnnotatedString {
                        withLink(LinkAnnotation.Url(url = stringResource(R.string.privacy_url))) {
                            append(stringResource(R.string.terms_of_use))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textDecoration = TextDecoration.Underline
                )
            }
            Button(
                onClick = {
                    subs.find {
                        it.id == selectedItem
                    }?.let {
                        onPurchase(it)
                    }
                },
                modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.purchase_now))
            }
            Button(
                onClick = {
                    onRestore()
                },
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.05f
                    ), contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.restore_purchase))
            }
        }
    }
}

@Composable
private fun PinItem(label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.checkmark_medium_1),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "",
            modifier = Modifier.size(20.dp)
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PremiumItem(
    item: PricesMenu?,
    isSelected: Boolean,
    time: String,
    onClick: () -> Unit
) {
    val contentColor =
        animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            animationSpec = tween(400)
        )
    val outlineColor =
        animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            animationSpec = tween(400)
        )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color = outlineColor.value)
            .clickable {
                onClick()

            }
            .padding(horizontal = 1.dp)
            .padding(top = 1.dp, bottom = 5.dp),
        contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.value
            )
            Text(
                text = item?.price ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor.value
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = PIXEL_9_PRO)
@Composable
private fun ScreenContentPreview(
) {

    StatusSaverTheme {
        PremiumContent(
            subs = listOf(
                PricesMenu(
                    price = "$20",
                    microPrice = 20 * 1000L,
                    currencyCode = "USD",
                    productId = BuildConfig.SUB_WEEKLY
                ),
                PricesMenu(
                    price = "$50",
                    microPrice = 50 * 1000L,
                    currencyCode = "USD",
                    productId = BuildConfig.SUB_MONTHLY
                ),
                PricesMenu(
                    price = "$100",
                    microPrice = 100 * 1000L,
                    currencyCode = "USD",
                    productId = BuildConfig.SUB_YEARLY
                )
            )
        )
    }
}