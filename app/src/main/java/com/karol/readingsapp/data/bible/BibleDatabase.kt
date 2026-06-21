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
        private const val ASSET_VERSION = 1

        fun getDatabase(context: Context): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = context.getDatabasePath("bibles.db")
                val prefs = context.getSharedPreferences("bible_db_prefs", Context.MODE_PRIVATE)
                val lastVersion = prefs.getInt("version", 0)

                if (!dbFile.exists() || (lastVersion < ASSET_VERSION)) {
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

            val requiredTables = listOf("translations", "verses", "books")
            for (table in requiredTables) {
                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$table'")
                val exists = cursor.moveToFirst()
                cursor.close()
                if (!exists) {
                    throw IllegalStateException("bibles.db is missing required table: $table")
                }
            }
        }
    }
}
