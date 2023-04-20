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


-keep class com.active.orbit.tracker.data_upload.dto.** {*;}

# database data classes
-keep class com.active.orbit.tracker.database.model.** {*;}

-keep class com.active.orbit.tracker.tracker.sensors.SensingData { *; }
-keep class com.active.orbit.tracker.tracker.sensors.step_counting.StepsData { *; }
-keep class com.active.orbit.tracker.tracker.sensors.location_recognition.LocationData { *; }
-keep class com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData { *; }
-keep class com.active.orbit.tracker.tracker.sensors.batteries.BatteryData { *; }
-keep class com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityData { *; }
-keep class com.active.orbit.tracker.retrieval.data.TripData { *; }
-keep class com.active.orbit.tracker.retrieval.data.SummaryData { *; }

-keep class com.active.orbit.tracker.data_upload.dts_data.ActivityDataDTS { *; }
-keep class com.active.orbit.tracker.data_upload.dts_data.HeartRateDataDTS { *; }
-keep class com.active.orbit.tracker.data_upload.dts_data.BatteryDataDTS { *; }
-keep class com.active.orbit.tracker.data_upload.dts_data.LocationDataDTS { *; }
-keep class com.active.orbit.tracker.data_upload.dts_data.StepsDataDTS { *; }
-keep class com.active.orbit.tracker.data_upload.dts_data.TripDataDTS{ *; }


# for GSonObjects
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}


# Gson classes
-keep class * extends com.google.gson.** {*;}

-keep class com.active.orbit.tracker.data_upload.dts_data.** {*;}


 ##-------- rules for removing Log methods in release
#-assumenosideeffects class android.util.Log {
#  public static boolean isLoggable(java.lang.String, int);
#  public static int v(...);
#  public static int i(...);
#  public static int w(...);
#  public static int d(...);
#  public static int e(...);
#}