package com.codecx.statussaver.core.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.codecx.statussaver.data.models.PricesMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingHelperNew(
    private val context: Context
) {
    private var billingClient: BillingClient? = null

    private var productDetailsMap: MutableMap<String, ProductDetails> = mutableMapOf()

    private val _purchaseUpdateEventListener =
        MutableStateFlow<PurchaseEvents>(PurchaseEvents.PurchaseInit)
    private val _offersUpdateListener = MutableStateFlow<List<PricesMenu>>(emptyList())

    val purchaseUpdateEventListener: StateFlow<PurchaseEvents> =
        _purchaseUpdateEventListener.asStateFlow()
    val offersUpdateListener: StateFlow<List<PricesMenu>> =
        _offersUpdateListener.asStateFlow()


    private var retryCount: Int = 0

    private fun initConnection(
        scope: CoroutineScope,
        subscriptions: List<String>, products: List<String>
    ) {

        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans()
                        .build()
                )
                .build()
        }
        startBillingConnection(scope, subscriptions, products)
    }

    private fun startBillingConnection(
        scope: CoroutineScope,
        subscriptions: List<String>,
        products: List<String>
    ) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    retryCount = 0

                    scope.launch {
                        launch {
                            val isPurchased = checkAllPurchases()

                            _purchaseUpdateEventListener.value = PurchaseEvents.OldPurchaseUpdate(
                                isPurchased
                            )
                        }
                        launch {

                            queryDetails(subscriptions, BillingClient.ProductType.SUBS)
                        }
                        launch {
                            queryDetails(products, BillingClient.ProductType.INAPP)
                        }

                    }
                }
            }

            override fun onBillingServiceDisconnected() {

                if (retryCount < 3) {
                    retryCount++
                    scope.launch {
                        delay(2000)
                        startBillingConnection(scope, subscriptions, products)
                    }
                }
            }
        })
    }

    suspend fun restorePurchases() = coroutineScope {

        val subsDeferred = async { queryAndRestore(BillingClient.ProductType.SUBS) }
        val inAppDeferred = async { queryAndRestore(BillingClient.ProductType.INAPP) }

        val results = awaitAll(subsDeferred, inAppDeferred)
        val isPurchased = results.any { it }
        _purchaseUpdateEventListener.value = (PurchaseEvents.OldPurchaseUpdate(isPurchased))

        isPurchased
    }

    private suspend fun checkAllPurchases(): Boolean = coroutineScope {
        val subsDeferred = async { checkPurchased(BillingClient.ProductType.SUBS) }
        val inAppDeferred = async { checkPurchased(BillingClient.ProductType.INAPP) }
        val results = awaitAll(subsDeferred, inAppDeferred)
        results.any { it }
    }

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }

    private fun handlePurchase(purchase: Purchase?) {
        if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                        .build()
                billingClient?.acknowledgePurchase(
                    acknowledgePurchaseParams,
                    acknowledgePurchaseResponseListener
                )
            } else {
                context.showToast("Already subscribed")
            }
        }
    }

    private fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val acknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { billing ->
            if (billing.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchaseUpdateEventListener.value = (PurchaseEvents.PurchaseDone)
            }
        }

    private suspend fun checkPurchased(productType: String): Boolean =
        suspendCoroutine { continuation ->
            val client = billingClient
            if (client == null || !client.isReady) {
                continuation.resume(false)
            } else {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(productType)
                    .build()

                client.queryPurchasesAsync(params) { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(purchases.any { it.isAcknowledged })
                    } else {
                        continuation.resume(false)
                    }
                }
            }

        }

    private suspend fun queryAndRestore(productType: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || !client.isReady) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            val params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()

            client.queryPurchasesAsync(params) { billingResult, purchases ->

                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    continuation.resume(false)
                    return@queryPurchasesAsync
                }

                var restored = false

                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        restored = true

                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    }
                }

                continuation.resume(restored)
            }
        }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("Billing", "Purchase acknowledged")
            }
        }
    }

    private fun queryDetails(productIdList: List<String>, productType: String) {
        if (billingClient?.isReady == false || productIdList.isEmpty()) {
            return
        }

        val productList = productIdList.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetail ->
            val productDetailsList = productDetail.productDetailsList

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                productDetailsList.forEach {
                    productDetailsMap[it.productId] = it
                }
                if (productType == BillingClient.ProductType.SUBS) {
                    handleSubsDetails(productDetailsList)
                } else {
                    handleInAppDetails(productDetailsList)
                }
            }
        }
    }

    private val prices = mutableMapOf<String, List<PricesMenu>>()

    private fun handleSubsDetails(list: List<ProductDetails>) {
        list.forEach { item ->
            val productId = item.productId
            val offerList = mutableListOf<PricesMenu>()

            item.subscriptionOfferDetails?.forEach { offer ->

                offer.pricingPhases.pricingPhaseList.firstOrNull()?.let { phase ->
                    offerList.add(
                        PricesMenu(
                            price = phase.formattedPrice,
                            microPrice = phase.priceAmountMicros,
                            currencyCode = phase.priceCurrencyCode,
                            id = offer.basePlanId,
                            productId = productId,
                            offerId = offer.offerId,
                            offerToken = offer.offerToken
                        )
                    )


                }

            }
            prices[productId] = offerList
        }
        _offersUpdateListener.value = (prices.values.flatten())
    }

    private fun handleInAppDetails(list: List<ProductDetails>) {

        list.forEach { item ->
            val productId = item.productId
            val offer = item.oneTimePurchaseOfferDetails

            if (offer != null) {
                prices[productId] = listOf(
                    PricesMenu(
                        price = offer.formattedPrice,
                        microPrice = offer.priceAmountMicros,
                        currencyCode = offer.priceCurrencyCode,
                        productId = productId,
                        id = productId
                    )
                )
            }
        }
        _offersUpdateListener.value = (prices.values.flatten())
    }

    private fun terminateBillingConnection() {
        billingClient?.endConnection()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    fun initBilling(subs: List<String>, products: List<String>) {
        initConnection(
            scope = coroutineScope,
            subscriptions = subs,
            products = products
        )
    }

    fun launchBilling(pricesMenu: PricesMenu, activity: Activity) {
        val mProduct = productDetailsMap[pricesMenu.productId]

        if (mProduct != null) {
            val productDetailsParams = if (mProduct.productType == BillingClient.ProductType.SUBS) {
                val offerToken = pricesMenu.offerToken
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(mProduct)
                        .setOfferToken(offerToken)
                        .build()
                )
            } else {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(mProduct)
                        .build()
                )
            }

            if (productDetailsParams.isNotEmpty()) {
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParams)
                    .build()
                billingClient?.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }

    fun closeConnection() {
        terminateBillingConnection()
    }


}