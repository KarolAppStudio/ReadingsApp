package com.karol.readingsapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReadingSchedule::class, BibleTranslation::class, Verse::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun combinedDao(): CombinedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create tables to match the entities we added for symbol resolution
                db.execSQL("CREATE TABLE IF NOT EXISTS `translations` (`code` TEXT NOT NULL, `language` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`code`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `verses` (`translation_code` TEXT NOT NULL, `book_id` INTEGER NOT NULL, `chapter` INTEGER NOT NULL, `verse_id` INTEGER NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`translation_code`, `book_id`, `chapter`, `verse_id`))")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
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
                .createFromAsset("readingplan.db")
                .addMigrations(MIGRATION_1_2)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
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
