plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.codecx.videodownloader"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.codecx.videodownloader"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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

    implementation ("com.airbnb.android:lottie:+")
}