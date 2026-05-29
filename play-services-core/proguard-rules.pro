# Proguard rules for play-services-core
# minifyEnabled is currently false; these rules apply if it is ever enabled.

# Keep all microG and GmsCore internal classes
-keep class org.microg.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep AIDL Binder and IInterface implementations
-keep class ** extends android.os.Binder { *; }
-keep interface ** extends android.os.IInterface { *; }

# Keep annotated classes used for reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Strip verbose logs from release builds (Log.d, Log.v, Log.i)
# Activate by setting minifyEnabled=true in build.gradle release block
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
