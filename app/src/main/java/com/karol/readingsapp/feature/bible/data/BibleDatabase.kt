package com.karol.readingsapp.feature.bible.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TranslationEntity::class, BookEntity::class, Verse::class],
    version = 9,
    exportSchema = false,
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        fun getDatabase(context: Context): BibleDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                BibleDatabase::class.java,
                "bibles.db",
            )
                .createFromAsset("bibles.db")
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Move validation to a background thread if it's too slow,
                            // or only run it once after installation/update.
                            validateDatabase(db)
                        }
                    },
                ).build()
            INSTANCE = instance
            instance
        }

        private fun validateDatabase(db: SupportSQLiteDatabase) {
            // Remove legacy translations if they exist
            db.execSQL(
                "DELETE FROM translations WHERE code IN ('POL', 'KHM', 'MIZ') OR language IN ('Polish', 'pl', 'Khmer', 'km')",
            )
            db.execSQL("DELETE FROM verses WHERE translation_code IN ('KHM', 'MIZ')")

            // Skip integrity check on every open to speed up loading
            // Only use it if you suspect corruption
            /*
            val integrityCursor = db.query("PRAGMA integrity_check")
            if (integrityCursor.moveToFirst()) {
                val result = integrityCursor.getString(0)
                if (result != "ok") {
                    integrityCursor.close()
                    throw IllegalStateException("bibles.db failed integrity check: $result")
                }
            }
            integrityCursor.close()
             */

            // Optimize population logic
            populateTranslations(db)

            val booksCountCursor = db.query("SELECT COUNT(*) FROM books")
            var count = 0
            if (booksCountCursor.moveToFirst()) {
                count = booksCountCursor.getInt(0)
            }
            booksCountCursor.close()

            if (count < 66) {
                db.execSQL("DELETE FROM books")
                populateBooks(db)
            }
        }

        private fun populateTranslations(db: SupportSQLiteDatabase) {
            val translations = listOf(
                arrayOf("ENG", "English", "English-ASV"),
                arrayOf("HIN", "Hindi", "Hindi Bible"),
                arrayOf("BAN", "Bangla", "Bangla Bible"),
                arrayOf("KAN", "Kannada", "Kannada Bible"),
                arrayOf("MAL", "Malayalam", "Malayalam Bible"),
                arrayOf("TAM", "Tamil", "Tamil Bible"),
                arrayOf("TEL", "Telugu", "Telugu Bible"),
                arrayOf("MIZO", "Mizo", "Mizo Bible"),
                arrayOf("FAR", "Farsi", "Farsi Bible"),
            )
            db.beginTransaction()
            try {
                for (t in translations) {
                    db.execSQL(
                        "INSERT OR IGNORE INTO translations (code, language, name) VALUES (?, ?, ?)",
                        t,
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        private fun populateBooks(db: SupportSQLiteDatabase) {
            val books = listOf(
                "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
                "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
                "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles",
                "Ezra", "Nehemiah", "Esther", "Job", "Psalms",
                "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah",
                "Jeremiah", "Lamentations", "Ezekiel", "Daniel",
                "Hosea", "Joel", "Amos", "Obadiah", "Jonah",
                "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai",
                "Zechariah", "Malachi", "Matthew", "Mark", "Luke",
                "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians",
                "Galatians", "Ephesians", "Philippians", "Colossians",
                "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy",
                "Titus", "Philemon", "Hebrews", "James", "1 Peter",
                "2 Peter", "1 John", "2 John", "3 John", "Jude",
                "Revelation",
            )
            val osis = listOf(
                "Gen", "Exod", "Lev", "Num", "Deut", "Josh", "Judg", "Ruth", "1Sam", "2Sam",
                "1Kgs", "2Kgs", "1Chr", "2Chr", "Ezra", "Neh", "Esth", "Job", "Ps", "Prov",
                "Eccl", "Song", "Isa", "Jer", "Lam", "Ezek", "Dan", "Hos", "Joel", "Amos",
                "Obad", "Jonah", "Mic", "Nah", "Hab", "Zeph", "Hag", "Zech", "Mal", "Matt",
                "Mark", "Luke", "John", "Acts", "Rom", "1Cor", "2Cor", "Gal", "Eph", "Phil",
                "Col", "1Thess", "2Thess", "1Tim", "2Tim", "Titus", "Phlm", "Heb", "Jas", "1Pet",
                "2Pet", "1John", "2John", "3John", "Jude", "Rev",
            )

            db.beginTransaction()
            try {
                for (i in books.indices) {
                    val testament = if (i < 39) "OT" else "NT"
                    db.execSQL(
                        "INSERT INTO books (id, osis_code, name, testament, sort_order) VALUES (?, ?, ?, ?, ?)",
                        arrayOf<Any>(i, osis[i], books[i], testament, i),
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
