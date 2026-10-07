# FocusFlow ProGuard / R8 Rules for Release Builds

# --- General Serialization & Reflection ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# --- Room Persistence Library ---
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.focusflow.core.data.local.entity.** { *; }
-keep interface com.focusflow.core.data.local.dao.** { *; }
-dontwarn androidx.room.paging.**

# --- Pure Domain Models (Serialization & Reflection safety) ---
-keep class com.focusflow.core.domain.model.** { *; }
-keep class com.focusflow.core.domain.blocking.model.** { *; }
-keep class com.focusflow.core.domain.social.model.** { *; }
-keep class com.focusflow.core.domain.auth.model.** { *; }
-keep class com.focusflow.core.domain.usage.model.** { *; }
-keep class com.focusflow.core.domain.permission.** { *; }

# --- Firebase (Firestore, Auth, Realtime Database) ---
-keepattributes *Annotation*,Signature
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <fields>;
}
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.database.** { *; }

# --- Kotlin Coroutines & Flow ---
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# --- Dagger / Hilt ---
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Third-party UI / Media (Coil, Vico) ---
-dontwarn coil3.**
-dontwarn com.patrykandpatrick.vico.**
