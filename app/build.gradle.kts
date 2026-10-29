import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.codecx.videodownloader"
    compileSdk = 37
    signingConfigs {
        create("release") {
            val keystorePropertiesFile =
                rootProject.file("videodownloader.jks")
            if (keystorePropertiesFile.exists()) {
                storeFile = keystorePropertiesFile
                storePassword = "videodownloader"
                keyAlias = "key0"
                keyPassword = "videodownloader"
            } else {
                println("Warning: keystore.properties not found. Release build may fail.")
            }
        }
    }
    defaultConfig {
        applicationId = "com.video.downloader.hd.fourk.videoplayer"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "10.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "weeklyPlanId", "\"weekly_price\"")
        buildConfigField("String", "weeklyBasePlanId", "\"weekly-subs\"")
        buildConfigField("String", "weeklyFreeTrailId", "\"weekly-trail\"")
    }

    base.archivesName.set("VideoDownloader_${defaultConfig.versionName}")
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
            freeCompilerArgs.add("-Xskip-prerelease-check")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    flavorDimensions += "mainFlavorDimension"
    productFlavors {
        create("dev") {
            dimension = "mainFlavorDimension"


            buildConfigField("String", "app_id", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "banner_id", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField(
                "String",
                "home_inter_id",
                "\"ca-app-pub-3940256099942544/1033173712\""
            )
            buildConfigField(
                "String",
                "home_native_id",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )
            buildConfigField(
                "String",
                "lang_native_id",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )
            buildConfigField(
                "String",
                "onboarding_finish_inter_id",
                "\"ca-app-pub-3940256099942544/1033173712\""
            )
            buildConfigField(
                "String",
                "onboarding_native_id",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )
            buildConfigField(
                "String",
                "premium_inter_id",
                "\"ca-app-pub-3940256099942544/1033173712\""
            )
            buildConfigField(
                "String",
                "resume_open_id",
                "\"ca-app-pub-3940256099942544/9257395921\""
            )
            buildConfigField(
                "String",
                "splash_open_id",
                "\"ca-app-pub-3940256099942544/9257395921\""
            )
            buildConfigField(
                "String",
                "splash_inter_id",
                "\"ca-app-pub-3940256099942544/1033173712\""
            )
            buildConfigField(
                "String",
                "splash_native_id",
                "\"ca-app-pub-3940256099942544/2247696110\""
            )

        }
        create("pro") {
            dimension = "mainFlavorDimension"

            buildConfigField("String", "app_id", "\"ca-app-pub-8108236472986627~4477104468\"")
            buildConfigField("String", "banner_id", "\"ca-app-pub-8108236472986627/6172882401\"")
            buildConfigField(
                "String",
                "home_inter_id",
                "\"ca-app-pub-8108236472986627/6147252662\""
            )
            buildConfigField(
                "String",
                "home_native_id",
                "\"ca-app-pub-8108236472986627/9414570520\""
            )
            buildConfigField(
                "String",
                "lang_native_id",
                "\"ca-app-pub-8108236472986627/9136378088\""
            )
            buildConfigField(
                "String",
                "onboarding_finish_inter_id",
                "\"ca-app-pub-8108236472986627/1198178317\""
            )
            buildConfigField(
                "String",
                "onboarding_native_id",
                "\"ca-app-pub-8108236472986627/8607474051\""
            )
            buildConfigField(
                "String",
                "premium_inter_id",
                "\"ca-app-pub-8108236472986627/4722490356\""
            )
            buildConfigField(
                "String",
                "resume_open_id",
                "\"ca-app-pub-8108236472986627/3946621428\""
            )
            buildConfigField(
                "String",
                "splash_open_id",
                "\"ca-app-pub-8108236472986627/6364454093\""
            )
            buildConfigField(
                "String",
                "splash_inter_id",
                "\"ca-app-pub-8108236472986627/4828011145\""
            )
            buildConfigField(
                "String",
                "splash_native_id",
                "\"ca-app-pub-8108236472986627/2633539756\""
            )
        }
    }
}

dependencies {
    implementation(project(":ESSStatusSaver"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.ui.test)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //GooglePlay
    implementation("com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.2.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")

    //Ktor
    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-okhttp:3.2.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
    implementation("io.ktor:ktor-client-logging:3.2.3")
    implementation("io.ktor:ktor-serialization-gson:3.2.3")

    //InAppUpdate
    implementation("com.google.android.play:app-update-ktx:2.1.0")


    //Glide
    implementation("com.github.bumptech.glide:glide:5.0.5")


    //LifeCycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")


    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-inappmessaging-display")

    //ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")

    // Gson (JSON parsing)
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("androidx.work:work-runtime-ktx:2.10.1")

    implementation("com.android.billingclient:billing-ktx:8.3.0")

    implementation("com.airbnb.android:lottie:+")
}
androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        val shouldEnableProGuard =
            variantBuilder.flavorName == "pro" && variantBuilder.buildType == "release"
        variantBuilder.isMinifyEnabled = shouldEnableProGuard
        variantBuilder.shrinkResources = shouldEnableProGuard
    }
}