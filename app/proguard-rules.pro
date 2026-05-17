# Keep Room, Hilt, and Compose metadata.
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

-keepclassmembers class ** {
  @androidx.room.* <methods>;
}

