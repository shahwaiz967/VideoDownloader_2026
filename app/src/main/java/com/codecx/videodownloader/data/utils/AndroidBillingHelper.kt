package com.codecx.videodownloader.data.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
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
import com.codecx.videodownloader.data.models.PricesMenu
import com.codecx.videodownloader.data.models.PurchaseEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

class AndroidBillingHelper(
    private val context: Context
) : BillingHelper, Application.ActivityLifecycleCallbacks {

    private var billingClient: BillingClient? = null

    private var productDetailsMap: MutableMap<String, ProductDetails> = mutableMapOf()

    private val _purchaseUpdateEventListener =
        MutableStateFlow<PurchaseEvents>(PurchaseEvents.PurchaseInit)
    private val _offersUpdateListener = MutableStateFlow<List<PricesMenu>>(emptyList())

    override val purchaseUpdateEventListener: StateFlow<PurchaseEvents> =
        _purchaseUpdateEventListener
    override val offersUpdateListener: StateFlow<List<PricesMenu>> =
        _offersUpdateListener

    companion object {
        @Volatile
        private var INSTANCE: AndroidBillingHelper? = null

        fun getInstance(context: Context): AndroidBillingHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidBillingHelper(context).also {
                    INSTANCE = it
                }
            }
        }
    }

    private var retryCount: Int = 0

    private fun initConnection(
        scope: CoroutineScope,
        subscriptions: List<String>, products: List<String>
    ) {
        "AndroidBilling".printLog("Init")

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
                    "AndroidBilling".printLog("onBillingSetupFinished: ${billingResult.responseCode} --- ${billingResult.debugMessage}")
                    scope.launch {
                        launch {
                            val isPurchased = checkAllPurchases()
                            "AndroidBilling".printLog("isPurchased: ${isPurchased}")

                            _purchaseUpdateEventListener.value =
                                PurchaseEvents.OldPurchaseUpdate(
                                    isPurchased
                                )
                        }
                        launch {
                            "AndroidBilling".printLog("queryDetails: SUBS")

                            queryDetails(subscriptions, BillingClient.ProductType.SUBS)
                        }
                        launch {
                            "AndroidBilling".printLog("queryDetails: INAPP")
                            queryDetails(products, BillingClient.ProductType.INAPP)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                "AndroidBilling".printLog("onBillingServiceDisconnected")

                if (retryCount < 3) {
                    retryCount++
                    scope.launch {
                        delay(2000.milliseconds)
                        startBillingConnection(scope, subscriptions, products)
                    }
                }
            }
        })
    }

    override suspend fun restorePurchases() = coroutineScope {

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
            "AndroidBilling".printLog("queryDetails: Not Ready")
            return
        }

        val productList = productIdList.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        "AndroidBilling".printLog("queryDetails:  queryProductDetailsAsync")

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetail ->
            val productDetailsList = productDetail.productDetailsList
            "AndroidBilling".printLog("${billingResult.responseCode} --- ${billingResult.debugMessage} --- ${productDetailsList.size}")

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
                "AndroidBillingInfo".printLog("isOfferToken: ${offer.offerId} ---- ${offer.offerToken}")

                offer.pricingPhases.pricingPhaseList.firstOrNull()?.let { phase ->
                    "AndroidBillingInfo".printLog("${offer.basePlanId} --- ${phase.formattedPrice}")
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

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun initBilling(subs: List<String>, products: List<String>) {
        "AndroidBilling".printLog("initBilling")
        initConnection(
            scope = coroutineScope,
            subscriptions = subs,
            products = products
        )
    }

    override fun launchBilling(pricesMenu: PricesMenu) {
        val activity = currentActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            "AndroidBilling".printLog("launchBilling: Activity is not valid")
            return
        }

        val client = billingClient
        if (client == null || !client.isReady) {
            "AndroidBilling".printLog("launchBilling: BillingClient is null or not ready")
            return
        }

        val mProduct = productDetailsMap[pricesMenu.productId]
        if (mProduct != null) {
            try {
                val productDetailsParams = if (mProduct.productType == BillingClient.ProductType.SUBS) {
                    val offerToken = pricesMenu.offerToken.ifEmpty {
                        mProduct.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                    }
                    if (offerToken.isEmpty()) {
                        "AndroidBilling".printLog("launchBilling: Offer token is empty for sub")
                        return
                    }
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

                    val billingResult = client.launchBillingFlow(activity, billingFlowParams)
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        "AndroidBilling".printLog("launchBillingFlow failed code: ${billingResult.responseCode}, msg: ${billingResult.debugMessage}")
                    }
                }
            } catch (e: Exception) {
                "AndroidBilling".printLog("launchBilling exception: ${e.message}")
                e.printStackTrace()
            }
        } else {
            "AndroidBilling".printLog("launchBilling: ProductDetails not found in map for ${pricesMenu.productId}")
        }
    }

    override fun closeConnection() {
        terminateBillingConnection()
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        currentActivity = activity
    }

    private var currentActivity: Activity? = null
    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
    }


}