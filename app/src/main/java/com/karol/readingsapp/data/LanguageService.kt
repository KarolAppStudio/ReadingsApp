package com.karol.readingsapp.data

import android.content.Context
import androidx.core.content.edit
import com.karol.readingsapp.data.bible.BibleDao
import com.karol.readingsapp.data.bible.Verse
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

class LanguageService(private val context: Context, private val bibleDao: BibleDao) {
    private val prefs = context.getSharedPreferences("language_downloads", Context.MODE_PRIVATE)
    private val _downloadStatus = MutableStateFlow<Map<String, LanguageStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _batchProgress = MutableStateFlow<Float?>(null)
    val batchProgress = _batchProgress.asStateFlow()

    private val baseUrl = "https://raw.githubusercontent.com/karol-bible/bible-data/main"

    init {
        // Load persisted download status
        val downloadedLanguages = prefs.all.keys.filter { prefs.getBoolean(it, false) }
        val statusMap = downloadedLanguages.associateWith { LanguageStatus.DOWNLOADED }.toMutableMap()

        // Ensure English is always considered downloaded (fallback)
        if (!statusMap.containsKey("English")) {
            statusMap["English"] = LanguageStatus.DOWNLOADED
            prefs.edit { putBoolean("English", true) }
        }
        _downloadStatus.value = statusMap
    }

    suspend fun downloadLanguageScript(language: String) = withContext(Dispatchers.IO) {
        if (_downloadStatus.value[language] == LanguageStatus.DOWNLOADED) return@withContext

        updateStatus(language, LanguageStatus.DOWNLOADING)

        val success = fetchAndStoreLanguage(language)
        if (success) {
            prefs.edit { putBoolean(language, true) }
            updateStatus(language, LanguageStatus.DOWNLOADED)
        } else {
            updateStatus(language, LanguageStatus.FAILED)
        }
    }

    suspend fun batchDownload(languages: List<String>) = withContext(Dispatchers.IO) {
        val toDownload = languages.filter { _downloadStatus.value[it] != LanguageStatus.DOWNLOADED }
        if (toDownload.isEmpty()) return@withContext

        _batchProgress.value = 0f

        toDownload.forEachIndexed { index, language ->
            updateStatus(language, LanguageStatus.DOWNLOADING)
            val success = fetchAndStoreLanguage(language)
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

    private suspend fun fetchAndStoreLanguage(language: String): Boolean = withContext(Dispatchers.IO) {
        val code = when (language) {
            "Hindi" -> "HIN"
            "Bangla" -> "BAN"
            "Kannada" -> "KAN"
            "Malayalam" -> "MAL"
            "Tamil" -> "TAM"
            "Telugu" -> "TEL"
            else -> "ENG"
        }

        // Try reading from assets first (included in APK)
        val assetPath = "bibles/$code.json"
        val jsonString = try {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }

        // Try reading from assets first (included in APK) or fallback to network
        jsonString?.let {
            return@withContext processJson(it, code)
        }
        fetchFromNetwork(code)
    }

    private suspend fun processJson(jsonString: String, code: String): Boolean = try {
        val jsonArray = JSONArray(jsonString)
        val verses = mutableListOf<Verse>()

        for (i in 0 until jsonArray.length()) {
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
        }

        if (verses.isNotEmpty()) {
            bibleDao.insertVerses(verses)
        }
        true
    } catch (e: Exception) {
        android.util.Log.e("LanguageService", "Error processing JSON for $code", e)
        false
    }

    private suspend fun fetchFromNetwork(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/$code.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                processJson(jsonString, code)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateStatus(language: String, status: LanguageStatus) {
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap[language] = status
        _downloadStatus.value = currentMap
    }
}
