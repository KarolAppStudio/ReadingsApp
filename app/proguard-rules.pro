# Keep Room entities and database classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep models and entities
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
}
