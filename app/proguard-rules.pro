# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep all fragments (needed for Navigation Component)
-keep public class com.google.android.gms.** { public protected *; }
-keep class com.google.android.material.focus.** { *; }
-dontwarn com.google.android.material.focus.**

-keep class * extends androidx.fragment.app.Fragment {
    <init>(...);
}

# Keep Navigation component generated classes
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

-keepclassmembers class * implements androidx.navigation.NavDirections {
    <methods>;
}
-keepclassmembers class * extends androidx.navigation.NavArgs {
    <methods>;
}

# Koin (Dependency Injection)
# Keeps annotations and classes needed for Koin to resolve dependencies at runtime.
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}
-keep class kotlin.reflect.KClass


# Firebase
# General rules for Firebase services.
   -keep class com.google.firebase.** { *; }
    -dontwarn com.google.firebase.**    # Firebase Analytics
    -keepclassmembers class com.google.firebase.analytics.FirebaseAnalytics {
        *** set*(***);
        *** log*(***);
    }

    # Firebase Crashlytics
    -keepattributes *Annotation*
    -keepattributes SourceFile,LineNumberTable
    -keep public class * extends java.lang.Exception



# Shimmer (Facebook Loading Animation)
-keep class com.facebook.shimmer.** { *; }
-dontwarn com.facebook.shimmer.**

-keep class com.bumptech.** { *; }


