package com.karol.readingsapp.feature.bible.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

enum class LanguageStatus {
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}

class LanguageService(private val context: Context, private val bibleDatabase: BibleDatabase) {
    private val bibleDao = bibleDatabase.bibleDao()
    private val prefs = context.getSharedPreferences("language_downloads", Context.MODE_PRIVATE)
    private val _downloadStatus = MutableStateFlow<Map<String, LanguageStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _individualProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val individualProgress = _individualProgress.asStateFlow()

    private val _batchProgress = MutableStateFlow<Float?>(null)
    val batchProgress = _batchProgress.asStateFlow()

    private val baseUrl = "https://raw.githubusercontent.com/KarolAppStudio/bible-data/main"
    private val remoteRepoApiUrl = "https://api.github.com/repos/KarolAppStudio/BibleTranslationDatabases/contents/"
    private val remoteDbBaseUrl = "https://raw.githubusercontent.com/KarolAppStudio/BibleTranslationDatabases/main"

    companion object {
        fun getNativeName(language: String, translationName: String): String {
            val lang = language.lowercase()
            return when {
                (lang.contains("hindi")) || (lang == "hi") || (lang == "hin") -> "हिन्दी"
                (lang.contains("bangla")) || (lang.contains("bengali")) || (lang == "bn") || (lang == "ban") -> "বাংলা"
                (lang.contains("kannada")) || (lang == "kn") || (lang == "kan") -> "ಕನ್ನಡ"
                (lang.contains("malayalam")) || (lang == "ml") || (lang == "mal") -> "മലയാളം"
                (lang.contains("tamil")) || (lang == "ta") || (lang == "tam") -> "தமிழ்"
                (lang.contains("telugu")) || (lang == "te") || (lang == "tel") -> "తెలుగు"
                translationName == "English-ASV" -> "English"
                else -> translationName.removeSuffix(" Bible")
            }
        }

        fun getLanguageCode(language: String): String = when (language) {
            "Hindi" -> "HIN"
            "Bangla" -> "BAN"
            "Kannada" -> "KAN"
            "Malayalam" -> "MAL"
            "Tamil" -> "TAM"
            "Telugu" -> "TEL"
            else -> "ENG"
        }
    }

    init {
        // Load persisted download status
        val downloadedLanguages = prefs.all.keys.filter { 
            it != "is_first_run" && it != "version" && prefs.all[it] is Boolean && prefs.getBoolean(it, false) 
        }
        val statusMap = downloadedLanguages.associateWith { LanguageStatus.DOWNLOADED }.toMutableMap()

        _downloadStatus.value = statusMap
    }

    suspend fun downloadLanguageScript(
        language: String,
        code: String? = null,
        force: Boolean = false,
        allowNetwork: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val currentStatus = _downloadStatus.value[language]
        if (!force && (currentStatus == LanguageStatus.DOWNLOADED || currentStatus == LanguageStatus.DOWNLOADING)) {
            return@withContext
        }

        updateStatus(language, LanguageStatus.DOWNLOADING)

        val success = fetchAndStoreLanguage(language, code, allowNetwork)
        if (success) {
            prefs.edit { putBoolean(language, true) }
            updateStatus(language, LanguageStatus.DOWNLOADED)
        } else {
            updateStatus(language, LanguageStatus.FAILED)
        }
    }

    suspend fun getRemoteTranslations(updateDb: Boolean = false): List<TranslationEntity> = withContext(Dispatchers.IO) {
        try {
            val url = URL(remoteRepoApiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "ReadingsApp")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val translations = mutableListOf<TranslationEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val fileName = obj.getString("name")
                    if (fileName.endsWith(".db")) {
                        val code = fileName.removeSuffix(".db")
                        val language = getLanguageFromCode(code)
                        val translation = TranslationEntity(
                            code = code,
                            language = language,
                            name = getTranslationName(code, language)
                        )
                        translations.add(translation)
                        if (updateDb) {
                            bibleDao.insertTranslation(translation)
                        }
                    }
                }
                translations
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("LanguageService", "Error fetching remote translations", e)
            emptyList()
        }
    }

    private fun getLanguageFromCode(code: String): String = when (code) {
        "ENG" -> "English"
        "HIN" -> "Hindi"
        "BAN" -> "Bangla"
        "KAN" -> "Kannada"
        "MAL" -> "Malayalam"
        "TAM" -> "Tamil"
        "TEL" -> "Telugu"
        else -> code
    }

    private fun getTranslationName(code: String, language: String): String = when (code) {
        "ENG" -> "English-ASV"
        else -> "$language Bible"
    }

    suspend fun batchDownload(
        languages: List<String>,
        codes: List<String?>? = null,
        force: Boolean = false,
        allowNetwork: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val toDownload = if (force) {
            languages.filter { _downloadStatus.value[it] != LanguageStatus.DOWNLOADING }
        } else {
            languages.filter { 
                val status = _downloadStatus.value[it]
                status != LanguageStatus.DOWNLOADED && status != LanguageStatus.DOWNLOADING 
            }
        }
        
        if (toDownload.isEmpty()) return@withContext

        _batchProgress.value = 0f

        toDownload.forEachIndexed { index, language ->
            val code = codes?.getOrNull(languages.indexOf(language))
            updateStatus(language, LanguageStatus.DOWNLOADING)
            val success = fetchAndStoreLanguage(language, code, allowNetwork)
            if (success) {
                prefs.edit { putBoolean(language, true) }
                updateStatus(language, LanguageStatus.DOWNLOADED)
            } else {
                updateStatus(language, LanguageStatus.FAILED)
            }
            _batchProgress.value = (index + 1).toFloat() / toDownload.size
        }

        _batchProgress.value = null
    }

    private suspend fun fetchAndStoreLanguage(
        language: String,
        forceCode: String? = null,
        allowNetwork: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        val code = forceCode ?: getLanguageCode(language)

        // Try reading from assets first (included in APK)
        val assetPath = "bibles/$code.json"
        val jsonString = try {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }

        // Try reading from assets first (included in APK) or fallback to network
        jsonString?.let {
            if (processJson(it, code)) return@withContext true
            android.util.Log.w("LanguageService", "Asset for $code found but failed to process (might be LFS pointer). Falling back to network.")
        }

        if (!allowNetwork) return@withContext false

        // Try downloading .db file first from BibleTranslations repo
        if (fetchDbFromNetwork(code)) {
            return@withContext true
        }

        fetchFromNetwork(code)
    }

    private suspend fun fetchDbFromNetwork(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val language = getLanguageFromCode(code)
            val url = URL("$remoteDbBaseUrl/$code.db")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val contentLength = connection.contentLength
                val tempFile = java.io.File(context.cacheDir, "${code}_temp.db")
                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                // 0.0 to 0.5 is download phase
                                updateProgress(language, (totalBytesRead.toFloat() / contentLength) * 0.5f)
                            }
                        }
                    }
                }
                val success = importFromDbFile(tempFile, code)
                tempFile.delete()
                success
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("LanguageService", "Error fetching DB from network for $code", e)
            false
        }
    }

    private suspend fun importFromDbFile(dbFile: java.io.File, code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val language = getLanguageFromCode(code)
            
            // Ensure the translation exists in the translations table first to satisfy FK constraint
            val translation = TranslationEntity(
                code = code,
                language = language,
                name = getTranslationName(code, language)
            )
            bibleDao.insertTranslation(translation)

            updateProgress(language, 0.6f)
            
            val db = bibleDatabase.openHelper.writableDatabase
            
            // Use ATTACH DATABASE to efficiently import data
            // We use [ ] or ' ' around the path to handle potential special characters
            db.execSQL("ATTACH DATABASE '${dbFile.absolutePath}' AS to_import")
            
            try {
                // Insert verses from the attached database into the main database
                // Room's table name is 'verses'
                db.execSQL("""
                    INSERT OR REPLACE INTO verses (book_id, chapter, verse, text, translation_code)
                    SELECT book_id, chapter, verse, text, '$code' FROM to_import.verses
                """.trimIndent())
                
                updateProgress(language, 0.9f)
            } finally {
                db.execSQL("DETACH DATABASE to_import")
            }
            
            updateProgress(language, 1.0f)
            true
        } catch (e: Exception) {
            android.util.Log.e("LanguageService", "Error importing from DB file for $code using ATTACH", e)
            // Fallback to manual import if ATTACH fails (e.g. schema mismatch in external DB)
            fallbackImportFromDbFile(dbFile, code)
        }
    }

    private suspend fun fallbackImportFromDbFile(dbFile: java.io.File, code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val language = getLanguageFromCode(code)
            val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            val cursor = db.rawQuery("SELECT book_id, chapter, verse, text FROM verses", null)
            val totalVerses = cursor.count
            val verses = mutableListOf<Verse>()
            var processedVerses = 0
            while (cursor.moveToNext()) {
                verses.add(
                    Verse(
                        bookId = cursor.getInt(0),
                        chapter = cursor.getInt(1),
                        verse = cursor.getInt(2),
                        text = cursor.getString(3),
                        translationCode = code
                    )
                )
                processedVerses++
                if (processedVerses % 1000 == 0 || processedVerses == totalVerses) {
                    updateProgress(language, 0.6f + (processedVerses.toFloat() / totalVerses) * 0.3f)
                }
            }
            cursor.close()
            db.close()

            if (verses.isNotEmpty()) {
                bibleDao.insertVerses(verses)
                updateProgress(language, 1.0f)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("LanguageService", "Error in fallback import for $code", e)
            false
        }
    }

    private suspend fun processJson(jsonString: String, code: String): Boolean = try {
        val language = getLanguageFromCode(code)
        val jsonArray = JSONArray(jsonString)
        val verses = mutableListOf<Verse>()
        val total = jsonArray.length()

        for (i in 0 until total) {
            val obj = jsonArray.getJSONObject(i)
            verses.add(
                Verse(
                    bookId = obj.getInt("book_id"),
                    chapter = obj.getInt("chapter"),
                    verse = obj.getInt("verse"),
                    text = obj.getString("text"),
                    translationCode = code,
                ),
            )
            if (i % 1000 == 0 || i == total - 1) {
                // 0.5 to 0.9 is extraction phase for JSON
                updateProgress(language, 0.5f + (i.toFloat() / total) * 0.4f)
            }
        }

        if (verses.isNotEmpty()) {
            updateProgress(language, 0.95f)
            bibleDao.insertVerses(verses)
            updateProgress(language, 1.0f)
        }
        true
    } catch (e: Exception) {
        android.util.Log.e("LanguageService", "Error processing JSON for $code", e)
        false
    }

    private suspend fun fetchFromNetwork(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val language = getLanguageFromCode(code)
            val url = URL("$baseUrl/$code.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val contentLength = connection.contentLength
                val inputStream = connection.inputStream
                val jsonString = if (contentLength > 0) {
                    val buffer = ByteArray(8192)
                    val out = java.io.ByteArrayOutputStream()
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        updateProgress(language, (totalBytesRead.toFloat() / contentLength) * 0.5f)
                    }
                    out.toString("UTF-8")
                } else {
                    inputStream.bufferedReader().use { it.readText() }
                }
                processJson(jsonString, code)
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("LanguageService", "Error fetching from network for $code", e)
            false
        }
    }

    fun updateStatus(language: String, status: LanguageStatus) {
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap[language] = status
        _downloadStatus.value = currentMap
    }

    suspend fun removeLanguage(language: String, code: String) = withContext(Dispatchers.IO) {
        // Prevent removing core translations if needed (though English/Malayalam logic is elsewhere)
        if (code == "ENG" || code == "MAL") return@withContext

        bibleDao.deleteVersesByTranslation(code)
        prefs.edit { remove(language) }
        
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap.remove(language)
        _downloadStatus.value = currentMap
        
        val progressMap = _individualProgress.value.toMutableMap()
        progressMap.remove(language)
        _individualProgress.value = progressMap
    }

    fun hasAsset(language: String): Boolean {
        val code = getLanguageCode(language)
        return try {
            val inputStream = context.assets.open("bibles/$code.json")
            val buffer = ByteArray(100)
            val read = inputStream.read(buffer)
            inputStream.close()

            if (read > 0) {
                val content = String(buffer, 0, read)
                // Check if it's a Git LFS pointer
                !content.startsWith("version https://git-lfs.github.com/spec/v1")
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun updateProgress(language: String, progress: Float) {
        val currentMap = _individualProgress.value.toMutableMap()
        currentMap[language] = progress
        _individualProgress.value = currentMap
    }
}
