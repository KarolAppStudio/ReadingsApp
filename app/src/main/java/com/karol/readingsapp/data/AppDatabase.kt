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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("ATTACH DATABASE '${attachedDbFile.absolutePath}' AS bible_db")

                            // Check if the reading_schedule table is empty
                            val cursor = db.query("SELECT COUNT(*) FROM reading_schedule")
                            if (cursor.moveToFirst()) {
                                val count = cursor.getInt(0)
                                if (count == 0) {
                                    // Prepopulate with sample data for June 2026 if empty
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
                                        "'2026-06-22', 39, 'Matthew', 8, 'Third Reading'"
                                    )
                                    sampleData.forEachIndexed { index, values ->
                                        db.execSQL("INSERT INTO reading_schedule (id, date, bookId, bookName, chapter, readingType) VALUES (${index + 1}, $values)")
                                    }
                                }
                            }
                            cursor.close()
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
