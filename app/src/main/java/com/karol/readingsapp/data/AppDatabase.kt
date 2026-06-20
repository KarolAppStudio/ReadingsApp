package com.karol.readingsapp.data

import android.content.Context
import androidx.core.content.edit
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReadingPlan::class, BibleTranslation::class, Verse::class],
    version = 6,
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `reading_schedule`")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `bible_companion`")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // We changed the columns of reading_plan, but since we are using createFromAsset,
                // Room might handle it if we are fresh, but for migration:
                db.execSQL("DROP TABLE IF EXISTS `reading_plan`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val attachedDbFile = context.getDatabasePath("bibles.db")
                val prefs = context.getSharedPreferences("db_prefs", Context.MODE_PRIVATE)
                val lastAssetVersion = prefs.getInt("bibles_db_version", 0)

                // Ensure the external bibles.db is copied and up to date
                if ((!attachedDbFile.exists()) || (lastAssetVersion < BIBLES_DB_ASSET_VERSION)) {
                    context.assets.open("bibles.db").use { input ->
                        attachedDbFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    prefs.edit { putInt("bibles_db_version", BIBLES_DB_ASSET_VERSION) }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readingplan.db",
                )
                .createFromAsset("readingplan.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            
                            // 1. Attach the external database
                            db.execSQL("ATTACH DATABASE '${attachedDbFile.absolutePath}' AS bible_db")

                            // 2. Validate integrity and schema of the attached database
                            validateExternalDatabase(db)
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
    }
}

