package com.karol.readingsapp.feature.plan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReadingPlanEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class ReadingPlanDatabase : RoomDatabase() {
    abstract fun readingPlanDao(): ReadingPlanDao

    companion object {
        @Volatile
        private var INSTANCE: ReadingPlanDatabase? = null
        private const val ASSET_VERSION = 3

        fun getDatabase(context: Context): ReadingPlanDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ReadingPlanDatabase::class.java,
                "readingplan.db",
            )
                .createFromAsset("readingplan.db")
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            validateDatabase(db)
                        }
                    },
                )
                .build()
            INSTANCE = instance
            instance
        }

        private fun validateDatabase(db: SupportSQLiteDatabase) {
            val cursor = db.query("SELECT COUNT(*) FROM reading_plan")
            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            android.util.Log.d("ReadingPlanDatabase", "Reading plan count: $count")

            if (count == 0) {
                // If the table is empty despite copying the asset, something is wrong.
                // We might need to force a re-copy next time or handle it.
                android.util.Log.e("ReadingPlanDatabase", "Reading plan table is empty!")
            }
        }
    }
}
