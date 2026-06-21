package com.karol.readingsapp.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LanguageStatus {
    NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, ERROR
}

class LanguageService() {
    private val _downloadStatus = MutableStateFlow<Map<String, LanguageStatus>>(emptyMap())
    val downloadStatus = _downloadStatus.asStateFlow()

    init {
        // Initialize with default English as downloaded
        _downloadStatus.value = mapOf("English" to LanguageStatus.DOWNLOADED)
    }

    suspend fun downloadLanguageScript(language: String) {
        if (_downloadStatus.value[language] == LanguageStatus.DOWNLOADED) return

        _downloadStatus.value = _downloadStatus.value + (language to LanguageStatus.DOWNLOADING)
        
        // Simulate network delay for downloading language script (JSON/Font/Mapping)
        delay(2000)
        
        // In a real app, this would fetch from a URL and store in local DB or file
        _downloadStatus.value = _downloadStatus.value + (language to LanguageStatus.DOWNLOADED)
    }

    fun isLanguageAvailable(language: String): Boolean {
        return _downloadStatus.value[language] == LanguageStatus.DOWNLOADED
    }
}
