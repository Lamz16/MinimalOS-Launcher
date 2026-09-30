# ProGuard / R8 rules for MinimalOS Launcher

# Keep launcher MainActivity
-keep class com.lamz.MainActivity { *; }

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# AndroidX DataStore
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    *;
}

