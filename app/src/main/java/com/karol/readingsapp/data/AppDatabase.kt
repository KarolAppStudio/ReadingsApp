package com.karol.readingsapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReadingSchedule::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun combinedDao(): CombinedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Ensure the secondary asset file is copied out to the system's database path 
                // so SQLite can see it locally for the ATTACH command
                val attachedDbFile = context.getDatabasePath("bibles.db")
                if (!attachedDbFile.exists()) {
                    context.assets.open("bibles.db").use { input ->
                        attachedDbFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readingplan.db",
                )
                .createFromAsset("readingplan.db") // Instantiates primary file from assets
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Attaching bibles.db under the alias 'bible_db'
                            db.execSQL("ATTACH DATABASE '${attachedDbFile.absolutePath}' AS bible_db")
                        }
                    },
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
