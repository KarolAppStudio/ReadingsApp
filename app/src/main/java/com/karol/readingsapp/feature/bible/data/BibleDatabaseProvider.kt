package com.karol.readingsapp.feature.bible.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class BibleDatabaseProvider(private val context: Context) {
    private val activeDatabase = MutableStateFlow<BibleDatabase?>(null)
    private val currentTranslationCode = MutableStateFlow<String?>(null)

    private val mutex = Mutex()
    private val databasesDir = File(context.filesDir, "downloaded_translations")

    init {
        if (!databasesDir.exists()) {
            databasesDir.mkdirs()
        }
    }

    suspend fun switchToTranslation(code: String) = mutex.withLock {
        if ((currentTranslationCode.value == code) && (activeDatabase.value != null)) {
            return@withLock
        }

        // Cleanly close the old instance
        activeDatabase.value?.close()
        activeDatabase.value = null

        val dbFile = File(databasesDir, "$code.db")
        if (!dbFile.exists()) {
            // If the file doesn't exist in our custom directory, it might be in assets or not downloaded
            // For now, we assume LanguageService handles the download.
            // If it's a core translation, it might be handled differently,
            // but the requirement says to build from the downloaded file.
            currentTranslationCode.value = null
            return@withLock
        }

        val db = Room.databaseBuilder(
            context,
            BibleDatabase::class.java,
            "bible_$code.db",
        )
            .createFromFile(dbFile)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

        activeDatabase.value = db
        currentTranslationCode.value = code
    }

    fun getDao(): BibleDao? = activeDatabase.value?.bibleDao()
}
