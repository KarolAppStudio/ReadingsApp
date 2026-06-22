package com.karol.readingsapp.data.plan

import android.content.Context
import androidx.core.content.edit
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

        fun getDatabase(context: Context): ReadingPlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = context.getDatabasePath("readingplan.db")
                val prefs = context.getSharedPreferences("reading_plan_prefs", Context.MODE_PRIVATE)
                val lastVersion = prefs.getInt("version", 0)

                android.util.Log.d("ReadingPlanDatabase", "dbFile path: ${dbFile.absolutePath}, exists: ${dbFile.exists()}, lastVersion: $lastVersion, ASSET_VERSION: $ASSET_VERSION")

                if (!dbFile.exists() || lastVersion < ASSET_VERSION) {
                    android.util.Log.d("ReadingPlanDatabase", "Copying database from assets...")
                    dbFile.parentFile?.mkdirs()
                    try {
                        context.assets.open("readingplan.db").use { input ->
                            val size = input.available()
                            android.util.Log.d("ReadingPlanDatabase", "Asset size: $size bytes")
                            dbFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        prefs.edit { putInt("version", ASSET_VERSION) }
                        android.util.Log.d("ReadingPlanDatabase", "Copy successful")
                    } catch (e: Exception) {
                        android.util.Log.e("ReadingPlanDatabase", "Error copying readingplan.db", e)
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReadingPlanDatabase::class.java,
                    "readingplan.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        validateDatabase(db)
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
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
