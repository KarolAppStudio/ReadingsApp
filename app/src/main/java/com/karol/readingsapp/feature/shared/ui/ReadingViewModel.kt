package com.karol.readingsapp.feature.shared.ui

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.core.theme.AppTheme
import com.karol.readingsapp.feature.bible.data.BookEntity
import com.karol.readingsapp.feature.bible.data.ChapterReference
import com.karol.readingsapp.feature.bible.data.LanguageService
import com.karol.readingsapp.feature.bible.data.LanguageStatus
import com.karol.readingsapp.feature.bible.data.ReadingRepository
import com.karol.readingsapp.feature.bible.data.TargetReadingDetails
import com.karol.readingsapp.feature.bible.data.TranslationEntity
import com.karol.readingsapp.feature.plan.data.SimpleReading
import com.karol.readingsapp.feature.voice.data.VoiceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class ReadingViewModel(
    private val repository: ReadingRepository,
    private val languageService: LanguageService,
    private val voiceService: VoiceService,
    context: Context,
) : ViewModel() {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState = _uiState.asStateFlow()

    private val _monthlyPlan = MutableStateFlow<Map<String, List<SimpleReading>>>(emptyMap())
    val monthlyPlan = _monthlyPlan.asStateFlow()

    private val _availableTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val availableTranslations = _availableTranslations.asStateFlow()

    private val _downloadedTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val downloadedTranslations = _downloadedTranslations.asStateFlow()

    private val _remoteTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val remoteTranslations = _remoteTranslations.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _allBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    val allBooks = _allBooks.asStateFlow()

    private val _allChapters = MutableStateFlow<List<ChapterReference>>(emptyList())
    val allChapters = _allChapters.asStateFlow()

    private val _chapterVerses = MutableStateFlow<List<TargetReadingDetails>>(emptyList())
    val chapterVerses = _chapterVerses.asStateFlow()

    private val _isCurrentTranslationComplete = MutableStateFlow(true)
    val isCurrentTranslationComplete = _isCurrentTranslationComplete.asStateFlow()

    private val _secondChapterVerses = MutableStateFlow<List<TargetReadingDetails>>(emptyList())
    val secondChapterVerses = _secondChapterVerses.asStateFlow()

    private val _selectedTranslationCode = MutableStateFlow(prefs.getString("default_bible", "ENG") ?: "ENG")
    val selectedTranslationCode = _selectedTranslationCode.asStateFlow()

    private val _secondTranslationCode = MutableStateFlow("ENG")
    val secondTranslationCode = _secondTranslationCode.asStateFlow()

    private val _appTheme =
        MutableStateFlow(
            try {
                AppTheme.valueOf(prefs.getString("app_theme", AppTheme.SKY_BLUE.name) ?: AppTheme.SKY_BLUE.name)
            } catch (_: Exception) {
                AppTheme.SKY_BLUE
            },
        )
    val appTheme = _appTheme.asStateFlow()

    val downloadStatus = languageService.downloadStatus
    val individualProgress = languageService.individualProgress
    val batchProgress = languageService.batchProgress

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        loadTranslations()
        loadAllBooks()
        refreshRemoteTranslations(updateDb = false)

        // Observe download status to refresh translations list and check for TTS
        viewModelScope.launch {
            var previousStatus = languageService.downloadStatus.value
            languageService.downloadStatus.collect { currentStatus ->
                loadTranslations()

                // Check for newly downloaded languages to ensure TTS data is also available
                var newlyDownloadedDetected = false
                currentStatus.forEach { (lang, status) ->
                    if (status == LanguageStatus.DOWNLOADED && previousStatus[lang] != LanguageStatus.DOWNLOADED) {
                        checkTTSForLanguage(lang)
                        newlyDownloadedDetected = true
                    }
                }
                
                if (newlyDownloadedDetected) {
                    loadAllBooks()
                }

                previousStatus = currentStatus
            }
        }
    }

    private fun checkTTSForLanguage(languageName: String) {
        val locale = when (languageName) {
            "English" -> Locale.ENGLISH
            "Hindi" -> Locale.forLanguageTag("hi-IN")
            "Bangla" -> Locale.forLanguageTag("bn-IN")
            "Kannada" -> Locale.forLanguageTag("kn-IN")
            "Malayalam" -> Locale.forLanguageTag("ml-IN")
            "Tamil" -> Locale.forLanguageTag("ta-IN")
            "Telugu" -> Locale.forLanguageTag("te-IN")
            else -> null
        }
        locale?.let {
            voiceService.ensureLanguageInstalled(it)
        }
    }

    fun refreshRemoteTranslations(updateDb: Boolean = true) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val remote = languageService.getRemoteTranslations(updateDb = updateDb).map {
                val nativeName = LanguageService.getNativeName(it.language, it.name)
                it.copy(name = nativeName)
            }
            if (remote.isNotEmpty()) {
                _remoteTranslations.value = remote
            }
            if (updateDb) {
                loadTranslations()
            }
            _isRefreshing.value = false
        }
    }

    private fun loadAllBooks() {
        viewModelScope.launch {
            _allBooks.value = repository.getAllBooks()
            _allChapters.value = repository.getAllChapters()
        }
    }

    suspend fun getChapterCount(bookId: Int): Int = repository.getChapterCount(bookId)

    suspend fun getVerseCount(
        bookId: Int,
        chapter: Int,
    ): Int = repository.getVerseCount(bookId, chapter)

    private fun loadTranslations() {
        viewModelScope.launch {
            val currentCode = _selectedTranslationCode.value
            _isCurrentTranslationComplete.value = repository.isTranslationComplete(currentCode)

            val allTranslations =
                repository.getAvailableTranslations().map {
                    val nativeName = LanguageService.getNativeName(it.language, it.name)
                    val displayLanguage = if (it.language == "English-ASV") "English" else it.language
                    it.copy(name = nativeName, language = displayLanguage)
                }
            _availableTranslations.value = allTranslations

            val downloadedFromDb = repository.getDownloadedTranslations().map {
                val nativeName = LanguageService.getNativeName(it.language, it.name)
                val displayLanguage = if (it.language == "English-ASV" || it.name == "English-ASV") "English" else it.language
                it.copy(name = nativeName, language = displayLanguage)
            }

            val statusMap = languageService.downloadStatus.value
            val downloadedByStatus = allTranslations.filter {
                statusMap[it.language] == LanguageStatus.DOWNLOADED
            }

            _downloadedTranslations.value = (downloadedFromDb + downloadedByStatus).distinctBy { it.code }

            val isFirstRun = prefs.getBoolean("is_first_run", true)
            if (isFirstRun) {
                val defaultLanguages = listOf("English", "Malayalam")
                // Check if assets are available for default languages
                val allAssetsPresent = defaultLanguages.all { languageService.hasAsset(it) }
                
                // If assets are missing, we MUST allow network even on first run to have a working app
                startBatchDownload(defaultLanguages, allowNetwork = allAssetsPresent.not())
                
                setTheme(AppTheme.SKY_BLUE)
                prefs.edit { putBoolean("is_first_run", false) }

                // Ensure TTS is checked for default languages on first run
                defaultLanguages.forEach { checkTTSForLanguage(it) }
            } else {
                // Check if default translations are actually complete
                val defaultCodes = listOf("ENG", "MAL")
                val incompleteDefaults = defaultCodes.filter { code ->
                    !repository.isTranslationComplete(code)
                }.map { code -> if (code == "ENG") "English" else "Malayalam" }

                // Only trigger repair if not already downloading
                val currentStatus = languageService.downloadStatus.value
                val actuallyIncomplete = incompleteDefaults.filter { lang ->
                    currentStatus[lang] != LanguageStatus.DOWNLOADING
                }

                if (actuallyIncomplete.isNotEmpty()) {
                    // If they are missing or incomplete after first run, we can try to repair them (allowing network now if necessary)
                    startBatchDownload(actuallyIncomplete, force = true)
                } else {
                    // Ensure default languages are eventually marked as downloaded if they weren't finished
                    val defaultLanguages = listOf("English", "Malayalam")
                    val missingDefaults = defaultLanguages.filter { lang ->
                        statusMap[lang] != LanguageStatus.DOWNLOADED &&
                        statusMap[lang] != LanguageStatus.DOWNLOADING &&
                        statusMap[lang] != LanguageStatus.FAILED
                    }
                    if (missingDefaults.isNotEmpty()) {
                        startBatchDownload(missingDefaults)
                    }
                }
            }

            allTranslations.find { it.code == _selectedTranslationCode.value }?.let {
                if (statusMap[it.language] != LanguageStatus.DOWNLOADED) {
                    languageService.downloadLanguageScript(it.language)
                }
            }
        }
    }

    fun setTranslation(translationCode: String) {
        val translation = _availableTranslations.value.find { it.code == translationCode }
        translation?.let {
            viewModelScope.launch {
                languageService.downloadLanguageScript(it.language)
                _selectedTranslationCode.value = translationCode
                _isCurrentTranslationComplete.value = repository.isTranslationComplete(translationCode)
                prefs.edit { putString("default_bible", translationCode) }
                if (_currentDate.value.isNotEmpty()) {
                    loadReading(_currentDate.value)
                }
                loadTranslations()
            }
        }
    }

    fun startBatchDownload(
        languages: List<String>,
        codes: List<String>? = null,
        force: Boolean = false,
        allowNetwork: Boolean = true
    ) {
        viewModelScope.launch {
            languageService.batchDownload(languages, codes, force, allowNetwork)
            loadTranslations() // Refresh available and downloaded lists after download attempt
        }
    }

    fun removeTranslation(language: String, code: String) {
        viewModelScope.launch {
            languageService.removeLanguage(language, code)
            loadTranslations()
            loadAllBooks() // Refresh book list in case a translation was removed
        }
    }

    fun setTheme(theme: AppTheme) {
        _appTheme.value = theme
        prefs.edit { putString("app_theme", theme.name) }
    }

    fun loadReading(date: String) {
        _currentDate.value = date
        viewModelScope.launch {
            val readings = repository.getReadingsForDate(date, _selectedTranslationCode.value)
            _uiState.value = readings
        }
    }

    fun loadChapterVerses(
        bookId: Int,
        chapter: Int,
    ) {
        viewModelScope.launch {
            _chapterVerses.value = repository.getChapterVerses(bookId, chapter, _selectedTranslationCode.value)
        }
    }

    suspend fun getChapterVerses(
        bookId: Int,
        chapter: Int,
    ): List<TargetReadingDetails> = repository.getChapterVerses(bookId, chapter, _selectedTranslationCode.value)

    fun loadSecondChapterVerses(
        bookId: Int,
        chapter: Int,
        translationCode: String,
    ) {
        viewModelScope.launch {
            _secondTranslationCode.value = translationCode
            _secondChapterVerses.value = repository.getChapterVerses(bookId, chapter, translationCode)
        }
    }

    fun loadMonthlyPlan(month: String) {
        viewModelScope.launch {
            _monthlyPlan.value = repository.getReadingsForMonth(month)
        }
    }

    fun resetParallelReading(
        bookId: Int,
        chapter: Int,
    ) {
        viewModelScope.launch {
            // First grid remains the default language Bible (_selectedTranslationCode)
            // Second grid is strictly set to English
            _secondTranslationCode.value = "ENG"
            _chapterVerses.value = repository.getChapterVerses(bookId, chapter, _selectedTranslationCode.value)
            _secondChapterVerses.value = repository.getChapterVerses(bookId, chapter, "ENG")
        }
    }

    fun getNextPortion(currentType: String?): TargetReadingDetails? {
        if (currentType == null) return null
        val readings = _uiState.value
        val types = listOf("First Reading", "Second Reading", "Third Reading")
        val currentIndex = types.indexOf(currentType)
        val hasValidIndex = currentIndex != -1
        val isNotLast = currentIndex < (types.size - 1)
        if (hasValidIndex && isNotLast) {
            for (i in (currentIndex + 1) until types.size) {
                val nextReadings = readings[types[i]]
                if (!nextReadings.isNullOrEmpty()) {
                    return nextReadings.first()
                }
            }
        }
        return null
    }
}
