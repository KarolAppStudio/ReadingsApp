package com.karol.readingsapp.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

enum class LanguageStatus {
    DOWNLOADING,
    DOWNLOADED,
}

class LanguageService(context: Context) {
    private val prefs = context.getSharedPreferences("language_downloads", Context.MODE_PRIVATE)
    private val _downloadStatus = MutableStateFlow<Map<String, LanguageStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _batchProgress = MutableStateFlow<Float?>(null)
    val batchProgress = _batchProgress.asStateFlow()

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
        if (_downloadStatus.value[language] != null) return@withContext

        updateStatus(language, LanguageStatus.DOWNLOADING)

        // Simulate network delay for downloading language script (JSON/Font/Mapping)
        // In a real app, we would use Ktor/Retrofit here to fetch data
        // and then insert it into the BibleDatabase via ReadingRepository
        delay(2.seconds)

        // Persist the downloaded status
        prefs.edit { putBoolean(language, true) }
        updateStatus(language, LanguageStatus.DOWNLOADED)
    }

    suspend fun batchDownload(languages: List<String>) = withContext(Dispatchers.IO) {
        val toDownload = languages.filter { _downloadStatus.value[it] != LanguageStatus.DOWNLOADED }
        if (toDownload.isEmpty()) return@withContext

        _batchProgress.value = 0f

        toDownload.forEach { language ->
            updateStatus(language, LanguageStatus.DOWNLOADING)
        }

        // Simulate batch downloading concurrently or sequentially
        toDownload.forEachIndexed { index, language ->
            delay(1.seconds) // Simulated fetch time
            prefs.edit { putBoolean(language, true) }
            updateStatus(language, LanguageStatus.DOWNLOADED)
            _batchProgress.value = (index + 1).toFloat() / toDownload.size
        }

        delay(0.5.seconds) // Keep the 100% visible for a moment
        _batchProgress.value = null
    }

    private fun updateStatus(language: String, status: LanguageStatus) {
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap[language] = status
        _downloadStatus.value = currentMap
    }
}
