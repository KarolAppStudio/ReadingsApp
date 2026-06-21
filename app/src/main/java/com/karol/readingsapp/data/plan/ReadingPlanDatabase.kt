package com.karol.readingsapp.data.plan

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ReadingPlanEntity::class],
    version = 6,
    exportSchema = false,
)
abstract class ReadingPlanDatabase : RoomDatabase() {
    abstract fun readingPlanDao(): ReadingPlanDao

    companion object {
        @Volatile
        private var INSTANCE: ReadingPlanDatabase? = null

        fun getDatabase(context: Context): ReadingPlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReadingPlanDatabase::class.java,
                    "readingplan.db"
                )
                .createFromAsset("readingplan.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
