# Proguard/R8 rules for play-services-core (minifyEnabled=true for release)

# ── microG / GmsCore ─────────────────────────────────────────────────────────
# Keep all app-level classes; they are accessed by external apps via AIDL and
# reflection, so shrinking them would break the GMS replacement contract.
-keep class org.microg.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.auth.** { *; }

# ── SafeParcel reflection ────────────────────────────────────────────────────
# SafeParcelReflectionUtil reads @SafeParceled field annotations at runtime.
# Keep annotated fields so their names and types survive obfuscation.
-keepclassmembers class * {
    @org.microg.safeparcel.SafeParceled *;
}

# ── Wire protobuf ────────────────────────────────────────────────────────────
-keep class com.squareup.wire.** { *; }
-keepclassmembers class * extends com.squareup.wire.Message {
    <fields>;
}

# ── AndroidX DataBinding ─────────────────────────────────────────────────────
# AGP 8+ injects DataBinding rules automatically, but be explicit for safety.
-keep class androidx.databinding.** { *; }
-keep class * extends androidx.databinding.ViewDataBinding { *; }

# ── AIDL / Binder ────────────────────────────────────────────────────────────
-keep class ** extends android.os.Binder { *; }
-keep interface ** extends android.os.IInterface { *; }

# ── Parcelable ───────────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── Kotlin metadata (needed for reflection and coroutines) ───────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Enums ────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Volley ───────────────────────────────────────────────────────────────────
-keep class com.android.volley.** { *; }

# ── Strip verbose logs from release APK ─────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
