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

# for GSonObjects
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

# Gson classes
-keep class * extends com.google.gson.** {*;}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

## Begin | proguard configuration for SQLChiper
-keep class net.sqlcipher.** { *; }
## End | proguard configuration for SQLChiper

## Begin | proguard configuration for project database models
-keep class uk.ac.shef.tracker.core.database.models.** { *; }
## End | proguard configuration for project database models

## Begin | proguard configuration for project serialization models
-keep class uk.ac.shef.tracker.core.serialization.** { *; }
## End | proguard configuration for project serialization models

## Begin | proguard configuration for project deserialization models
-keep class uk.ac.shef.tracker.core.deserialization.** { *; }
## End | proguard configuration for project deserialization models