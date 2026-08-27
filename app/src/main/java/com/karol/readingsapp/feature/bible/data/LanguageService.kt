package com.karol.readingsapp.feature.bible.data

import android.content.Context
import android.util.Log
import com.karol.readingsapp.core.i18n.Localization
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds

enum class LanguageStatus {
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}

class LanguageService(
    private val context: Context,
    bibleDatabase: BibleDatabase,
    dbProvider: BibleDatabaseProvider? = null, // Added for dynamic switching support
) {
    companion object {
        const val MAX_RETRIES = 3
        const val NETWORK_TIMEOUT = 15000 // 15 seconds

        fun getLanguageCode(language: String): String = when (language.lowercase()) {
            "english" -> "ENG"
            "hindi", "hi" -> "HIN"
            "bangla", "bn" -> "BAN"
            "kannada", "kn" -> "KAN"
            "malayalam", "ml", "mal" -> "MAL"
            "tamil", "ta" -> "TAM"
            "telugu", "te" -> "TEL"
            "mizo", "miz" -> "MIZ"
            "farsi", "fa" -> "FAR"
            else -> language.uppercase()
        }

        fun getNativeName(language: String, name: String): String = when (language.lowercase()) {
            "malayalam" -> "മലയാളം"
            "hindi" -> "हिन्दी"
            "bangla" -> "বাংলা"
            "kannada" -> "ಕನ್ನಡ"
            "tamil" -> "தமிழ்"
            "telugu" -> "తెలుగు"
            "mizo" -> "Mizo"
            "farsi" -> "فಾರಸಿ"
            else -> name
        }
    }

    private val bibleDao = bibleDatabase.bibleDao()
    private val prefs = context.getSharedPreferences("language_downloads", Context.MODE_PRIVATE)
    private val _downloadStatus = MutableStateFlow<Map<String, LanguageStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _individualProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val individualProgress = _individualProgress.asStateFlow()

    private val _batchProgress = MutableStateFlow<Float?>(null)
    val batchProgress = _batchProgress.asStateFlow()

    private val remoteRepoApiUrl = "https://api.github.com/repos/KarolAppStudio/BibleTranslationDatabases/contents/"
    private val remoteDbBaseUrl = "https://raw.githubusercontent.com/KarolAppStudio/BibleTranslationDatabases/main"
    private val remoteI18nBaseUrl = "https://raw.githubusercontent.com/KarolAppStudio/BibleTranslationDatabases/main/i18n"

    private val downloadedDir = File(context.filesDir, "downloaded_translations")
    private val i18nDir = File(context.filesDir, "downloaded_i18n")

    init {
        if (!downloadedDir.exists()) {
            downloadedDir.mkdirs()
        }
        if (!i18nDir.exists()) {
            i18nDir.mkdirs()
        }
        // Load existing dynamic localizations
        i18nDir.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
            try {
                Localization.registerDynamicLocalization(file.nameWithoutExtension, file.readText())
            } catch (_: Exception) {
            }
        }
        // Load persisted download status
        val downloadedLanguages = prefs.all.keys.asSequence().filter {
            (it != "is_first_run") && (it != "version") && (prefs.all[it] is Boolean) && prefs.getBoolean(it, false)
        }.toMutableSet()

        val statusMap = downloadedLanguages.associateWith { LanguageStatus.DOWNLOADED }.toMutableMap()

        // English and Malayalam are pre-packaged in the APK (via bibles.db asset)
        statusMap["English"] = LanguageStatus.DOWNLOADED
        statusMap["Malayalam"] = LanguageStatus.DOWNLOADED

        _downloadStatus.value = statusMap

        // Log dbProvider usage to suppress warning if we want to keep it as property,
        // but here I removed 'private val' from constructor.
        Log.d("LanguageService", "Initialised with dbProvider: ${dbProvider != null}")
    }

    suspend fun downloadLanguageScript(
        language: String,
        code: String? = null,
        force: Boolean = false,
        allowNetwork: Boolean = true,
    ) = withContext(Dispatchers.IO) {
        val currentStatus = _downloadStatus.value[language]
        if ((!force) && (currentStatus == LanguageStatus.DOWNLOADED)) {
            return@withContext
        }

        if (currentStatus == LanguageStatus.DOWNLOADING) {
            // Wait for completion
            while (_downloadStatus.value[language] == LanguageStatus.DOWNLOADING) {
                delay(100.milliseconds)
            }
            if (_downloadStatus.value[language] == LanguageStatus.DOWNLOADED) {
                return@withContext
            }
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

    suspend fun getRemoteTranslations(updateDb: Boolean = false): List<TranslationEntity> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(remoteRepoApiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = NETWORK_TIMEOUT
                connection.readTimeout = NETWORK_TIMEOUT
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
                                name = getTranslationName(code, language),
                            )
                            translations.add(translation)
                            if (updateDb) {
                                // For the main metadata DB
                                bibleDao.insertTranslation(translation)
                            }
                        }
                    }
                    translations
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("LanguageService", "Error fetching remote translations", e)
                emptyList()
            }
        }

    private fun getLanguageFromCode(code: String): String = when (code.uppercase()) {
        "ENG" -> "English"
        "HIN", "HI" -> "Hindi"
        "BAN", "BN" -> "Bangla"
        "KAN", "KN" -> "Kannada"
        "MAL", "ML" -> "Malayalam"
        "TAM", "TA" -> "Tamil"
        "TEL", "TE" -> "Telugu"
        "MIZO", "MIZ" -> "Mizo"
        "FAR" -> "Farsi"
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
        allowNetwork: Boolean = true,
    ) = withContext(Dispatchers.IO) {
        val toDownload = if (force) {
            languages.filter { _downloadStatus.value[it] != LanguageStatus.DOWNLOADING }
        } else {
            languages.filter {
                val status = _downloadStatus.value[it]
                ((status != LanguageStatus.DOWNLOADED) && (status != LanguageStatus.DOWNLOADING))
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
        allowNetwork: Boolean = true,
    ): Boolean = withContext(Dispatchers.IO) {
        val code = (forceCode ?: getLanguageCode(language)).let { if (it == "MIZ") "MIZO" else it }

        // 1. Try reading .db from assets first
        val dbAssetPath = "bibles/$code.db"
        val tempFile = File(context.cacheDir, "temp_asset_$code.db")
        try {
            context.assets.open(dbAssetPath).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                if ((tempFile.exists()) && (tempFile.length() > 0)) {
                    val destination = File(downloadedDir, "$code.db")
                    if (tempFile.renameTo(destination)) {
                        Log.d("LanguageService", "Successfully copied $code from assets")
                        return@withContext true
                    } else {
                        tempFile.copyTo(destination, overwrite = true)
                        tempFile.delete()
                        Log.d("LanguageService", "Successfully copied $code from assets (fallback copy)")
                        return@withContext true
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }

        if (!allowNetwork) return@withContext false

        // 2. Download .db file from network to cache, then move to filesDir
        fetchDbFromNetwork(code)
    }

    private suspend fun fetchDbFromNetwork(code: String): Boolean = withContext(Dispatchers.IO) {
        var attempt = 0
        var lastException: Exception? = null
        val tempFile = File(context.cacheDir, "temp_$code.db")

        while (attempt < MAX_RETRIES) {
            attempt++
            try {
                val language = getLanguageFromCode(code)
                val url = URL("$remoteDbBaseUrl/$code.db")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = NETWORK_TIMEOUT
                connection.readTimeout = NETWORK_TIMEOUT

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val contentLength = connection.contentLength

                    connection.inputStream.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                if (contentLength > 0) {
                                    updateProgress(language, (totalBytesRead.toFloat() / contentLength))
                                }
                            }
                            output.flush()
                        }
                    }

                    // Basic integrity check
                    if ((tempFile.exists()) && (tempFile.length() > 0)) {
                        val destination = File(downloadedDir, "$code.db")
                        if (tempFile.renameTo(destination)) {
                            downloadLocalization(code)
                            updateProgress(language, 1.0f)
                            return@withContext true
                        } else {
                            // Fallback if rename fails
                            tempFile.copyTo(destination, overwrite = true)
                            tempFile.delete()
                            downloadLocalization(code)
                            updateProgress(language, 1.0f)
                            return@withContext true
                        }
                    }
                } else if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    return@withContext false
                }
            } catch (e: Exception) {
                lastException = e
                Log.w("LanguageService", "Attempt $attempt failed for $code: ${e.message}")
                if (attempt < MAX_RETRIES) {
                    delay((2000L * attempt).milliseconds)
                }
            } finally {
                if (tempFile.exists()) tempFile.delete()
            }
        }
        Log.e("LanguageService", "All $MAX_RETRIES attempts failed for $code", lastException)
        false
    }

    private suspend fun downloadLocalization(code: String) = withContext(Dispatchers.IO) {
        try {
            val url = URL("$remoteI18nBaseUrl/$code.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = NETWORK_TIMEOUT
            connection.readTimeout = NETWORK_TIMEOUT

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val file = File(i18nDir, "$code.json")
                file.writeText(json)
                Localization.registerDynamicLocalization(code, json)
                Log.d("LanguageService", "Downloaded localization for $code")
            }
        } catch (e: Exception) {
            Log.w("LanguageService", "Failed to download localization for $code: ${e.message}")
        }
    }

    fun updateStatus(language: String, status: LanguageStatus) {
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap[language] = status
        _downloadStatus.value = currentMap
    }

    suspend fun removeLanguage(language: String, code: String) = withContext(Dispatchers.IO) {
        // Prevent removing core translations if needed (though English/Malayalam logic is elsewhere)
        if ((code == "ENG") || (code == "MAL")) return@withContext

        // Delete from main DB metadata if present
        bibleDao.deleteVersesByTranslation(code)

        // Delete the physical .db file
        val dbFile = File(downloadedDir, "$code.db")
        if (dbFile.exists()) {
            dbFile.delete()
        }

        // Delete the localization file
        val jsonFile = File(i18nDir, "$code.json")
        if (jsonFile.exists()) {
            jsonFile.delete()
        }

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
        // English and Malayalam are core assets bundled in bibles.db
        if (code == "ENG" || code == "MAL") return true

        // Check for both .db and .json assets
        val dbAssetPath = "bibles/$code.db"
        val jsonAssetPath = "bibles/$code.json"

        return try {
            context.assets.open(dbAssetPath).use { it.close() }
            true
        } catch (_: Exception) {
            try {
                context.assets.open(jsonAssetPath).use { it.close() }
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun updateProgress(language: String, progress: Float) {
        val currentMap = _individualProgress.value.toMutableMap()
        currentMap[language] = progress
        _individualProgress.value = currentMap
    }
}
