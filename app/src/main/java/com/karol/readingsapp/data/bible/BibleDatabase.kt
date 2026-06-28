package com.karol.readingsapp.data.bible

import android.content.Context
import androidx.core.content.edit
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TranslationEntity::class, BookEntity::class, Verse::class],
    version = 5,
    exportSchema = false,
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null
        private const val ASSET_VERSION = 5

        fun getDatabase(context: Context): BibleDatabase = INSTANCE ?: synchronized(this) {
// ... (omitting middle part for brevity, will use full block in tool)
            val dbFile = context.getDatabasePath("bibles.db")
            val prefs = context.getSharedPreferences("bible_db_prefs", Context.MODE_PRIVATE)
            val lastVersion = prefs.getInt("version", 0)

            if (!dbFile.exists() || (lastVersion < ASSET_VERSION)) {
                dbFile.parentFile?.mkdirs()
                context.assets.open("bibles.db").use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                prefs.edit { putInt("version", ASSET_VERSION) }
            }

            val instance = Room.databaseBuilder(
                context.applicationContext,
                BibleDatabase::class.java,
                "bibles.db",
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            validateDatabase(db)
                        }
                    },
                ).build()
            INSTANCE = instance
            instance
        }

        private fun validateDatabase(db: SupportSQLiteDatabase) {
            // Remove Polish translations if they exist
            db.execSQL("DELETE FROM translations WHERE language LIKE 'Polish' OR language = 'pl' OR code = 'POL'")

            val integrityCursor = db.query("PRAGMA integrity_check")
            if (integrityCursor.moveToFirst()) {
                val result = integrityCursor.getString(0)
                if (result != "ok") {
                    integrityCursor.close()
                    throw IllegalStateException("bibles.db failed integrity check: $result")
                }
            }
            integrityCursor.close()

            // Log all tables
            val allTablesCursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
            val tables = mutableListOf<String>()
            while (allTablesCursor.moveToNext()) {
                tables.add(allTablesCursor.getString(0))
            }
            allTablesCursor.close()
            android.util.Log.d("BibleDatabase", "All tables: ${tables.joinToString()}")

            // Log books schema and count
            val booksSchemaCursor = db.query("SELECT sql FROM sqlite_master WHERE name='books'")
            if (booksSchemaCursor.moveToFirst()) {
                android.util.Log.d("BibleDatabase", "Books schema: ${booksSchemaCursor.getString(0)}")
            }
            booksSchemaCursor.close()

            val booksCountCursor = db.query("SELECT COUNT(*) FROM books")
            var count = 0
            if (booksCountCursor.moveToFirst()) {
                count = booksCountCursor.getInt(0)
            }
            booksCountCursor.close()

            // Check if Genesis (ID 0) exists
            val genesisCursor = db.query("SELECT COUNT(*) FROM books WHERE id = 0")
            var hasGenesis = false
            if (genesisCursor.moveToFirst()) {
                hasGenesis = genesisCursor.getInt(0) > 0
            }
            genesisCursor.close()

            if ((count < 66) || !hasGenesis) {
                db.execSQL("DELETE FROM books")
                populateBooks(db)
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
