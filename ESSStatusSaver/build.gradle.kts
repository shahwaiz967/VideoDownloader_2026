import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("kotlinx-serialization")

}

android {
    namespace = "com.codecx.statussaver"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUB_MONTHLY", "\"sub_monthly\"")
        buildConfigField("String", "SUB_WEEKLY", "\"sub_weekly\"")
        buildConfigField("String", "SUB_YEARLY", "\"sub_yearly\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin{
         compilerOptions {
             languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
             freeCompilerArgs.add("-Xexplicit-backing-fields")
         }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.documentfile)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.billing)

    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.coil.gif)
    implementation(libs.accompanist.systemuicontroller)

}