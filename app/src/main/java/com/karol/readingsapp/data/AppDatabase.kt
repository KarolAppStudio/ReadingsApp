package com.karol.readingsapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReadingSchedule::class, BibleTranslation::class, Verse::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun combinedDao(): CombinedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val BIBLES_DB_ASSET_VERSION = 1 // Increment this when updating the asset

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `translations` (`code` TEXT NOT NULL, `language` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`code`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `verses` (`translation_code` TEXT NOT NULL, `book_id` INTEGER NOT NULL, `chapter` INTEGER NOT NULL, `verse` INTEGER NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`translation_code`, `book_id`, `chapter`, `verse`))")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `verses`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `verses` (`translation_code` TEXT NOT NULL, `book_id` INTEGER NOT NULL, `chapter` INTEGER NOT NULL, `verse` INTEGER NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`translation_code`, `book_id`, `chapter`, `verse`))")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val attachedDbFile = context.getDatabasePath("bibles.db")
                val prefs = context.getSharedPreferences("db_prefs", Context.MODE_PRIVATE)
                val lastAssetVersion = prefs.getInt("bibles_db_version", 0)

                // Ensure the external bibles.db is copied and up to date
                if (!attachedDbFile.exists() || lastAssetVersion < BIBLES_DB_ASSET_VERSION) {
                    context.assets.open("bibles.db").use { input ->
                        attachedDbFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    prefs.edit().putInt("bibles_db_version", BIBLES_DB_ASSET_VERSION).apply()
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readingplan.db",
                )
                .createFromAsset("readingplan.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            
                            // 1. Attach the external database
                            db.execSQL("ATTACH DATABASE '${attachedDbFile.absolutePath}' AS bible_db")

                            // 2. Validate integrity and schema of the attached database
                            validateExternalDatabase(db)

                            // 3. Prepopulate reading_schedule if empty
                            prepopulateIfEmpty(db)
                        }
                    },
                )
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun validateExternalDatabase(db: SupportSQLiteDatabase) {
            // Check physical integrity
            val integrityCursor = db.query("PRAGMA bible_db.integrity_check")
            if (integrityCursor.moveToFirst()) {
                val result = integrityCursor.getString(0)
                if (result != "ok") {
                    integrityCursor.close()
                    throw IllegalStateException("External bibles.db failed integrity check: $result")
                }
            }
            integrityCursor.close()

            // Validate required tables
            val requiredTables = listOf("translations", "verses")
            for (table in requiredTables) {
                val cursor = db.query("SELECT name FROM bible_db.sqlite_master WHERE type='table' AND name='$table'")
                val exists = cursor.moveToFirst()
                cursor.close()
                if (!exists) {
                    throw IllegalStateException("External bibles.db is missing required table: $table")
                }
            }
        }

        private fun prepopulateIfEmpty(db: SupportSQLiteDatabase) {
            val cursor = db.query("SELECT COUNT(*) FROM reading_schedule")
            if (cursor.moveToFirst()) {
                val count = cursor.getInt(0)
                if (count == 0) {
                    val sampleData = listOf(
                        "'2026-06-18', 0, 'Genesis', 17, 'First Reading'",
                        "'2026-06-18', 18, 'Psalms', 19, 'Second Reading'",
                        "'2026-06-18', 39, 'Matthew', 4, 'Third Reading'",
                        "'2026-06-19', 0, 'Genesis', 18, 'First Reading'",
                        "'2026-06-19', 18, 'Psalms', 20, 'Second Reading'",
                        "'2026-06-19', 39, 'Matthew', 5, 'Third Reading'",
                        "'2026-06-20', 0, 'Genesis', 19, 'First Reading'",
                        "'2026-06-20', 18, 'Psalms', 21, 'Second Reading'",
                        "'2026-06-20', 39, 'Matthew', 6, 'Third Reading'",
                        "'2026-06-21', 0, 'Genesis', 20, 'First Reading'",
                        "'2026-06-21', 18, 'Psalms', 22, 'Second Reading'",
                        "'2026-06-21', 39, 'Matthew', 7, 'Third Reading'",
                        "'2026-06-22', 0, 'Genesis', 21, 'First Reading'",
                        "'2026-06-22', 18, 'Psalms', 23, 'Second Reading'",
                        "'2026-06-22', 39, 'Matthew', 8, 'Third Reading'",
                    )
                    sampleData.forEachIndexed { index, values ->
                        db.execSQL("INSERT INTO reading_schedule (id, date, bookId, bookName, chapter, readingType) VALUES (${index + 1}, $values)")
                    }
                }
            }
            cursor.close()
        }
    }
}

