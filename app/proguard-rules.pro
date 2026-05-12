# ================================
# GENERAL
# ================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ================================
# HILT / DAGGER
# ================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# ================================
# RETROFIT
# ================================
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ================================
# GSON (MODEL CLASSES)
# ================================
-keep class com.sanju.newsapp.model.** { *; }
-keepattributes Signature

# ================================
# ROOM
# ================================
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# ================================
# GLIDE
# ================================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule

# ================================
# WEBVIEW (if using JS interface)
# ================================
# Uncomment if needed
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}

# ================================
# KOTLIN
# ================================
-keep class kotlin.Metadata { *; }

# ================================
# OKHTTP
# ================================
-dontwarn okhttp3.**
-dontwarn okio.**

# ================================
# OPTIONAL (DEBUGGING)
# ================================
# Keep line numbers for crash logs
-keepattributes SourceFile,LineNumberTable