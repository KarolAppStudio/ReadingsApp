package com.karol.readingsapp.feature.shared.ui

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.core.i18n.Localization
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AppTheme
import com.karol.readingsapp.core.update.AppUpdateManager
import com.karol.readingsapp.feature.bible.data.BibleDatabaseProvider
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class ReadingViewModel(
    private val repository: ReadingRepository,
    private val languageService: LanguageService,
    private val voiceService: VoiceService,
    private val dbProvider: BibleDatabaseProvider,
    context: Context,
) : ViewModel() {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val updateManager = AppUpdateManager(context)

    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState = _uiState.asStateFlow()

    private val _monthlyPlan = MutableStateFlow<Map<String, List<SimpleReading>>>(emptyMap())
    val monthlyPlan = _monthlyPlan.asStateFlow()

    private val _availableTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val availableTranslations = _availableTranslations.asStateFlow()

    private val _downloadedTranslations = MutableStateFlow(
        listOf(
            TranslationEntity("ENG", "English", "English"),
            TranslationEntity("MAL", "Malayalam", "മലയാളം"),
        ),
    )
    val downloadedTranslations = _downloadedTranslations.asStateFlow()

    private val _remoteTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val remoteTranslations = _remoteTranslations.asStateFlow()

    private val _isRefreshing = MutableStateFlow(value = false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _allBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    val allBooks = _allBooks.asStateFlow()

    private val _allChapters = MutableStateFlow<List<ChapterReference>>(emptyList())
    val allChapters = _allChapters.asStateFlow()

    private val _chapterVerses = MutableStateFlow<List<TargetReadingDetails>>(emptyList())
    val chapterVerses = _chapterVerses.asStateFlow()

    private val _isCurrentTranslationComplete = MutableStateFlow(value = true)
    val isCurrentTranslationComplete = _isCurrentTranslationComplete.asStateFlow()

    private val _secondChapterVerses = MutableStateFlow<List<TargetReadingDetails>>(emptyList())
    val secondChapterVerses = _secondChapterVerses.asStateFlow()

    private val _selectedTranslationCode = MutableStateFlow(
        if (prefs.getBoolean("is_first_run", true)) {
            "ENG"
        } else {
            prefs.getString("default_bible", "ENG") ?: "ENG"
        },
    )
    val selectedTranslationCode = _selectedTranslationCode.asStateFlow()

    val selectedLanguage: StateFlow<String> = combine(
        _selectedTranslationCode,
        _downloadedTranslations,
    ) { code, downloaded ->
        downloaded.find { it.code == code }?.language ?: "English"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "English",
    )

    val strings: StateFlow<LocalizedStrings> = selectedLanguage.combine(_downloadedTranslations) { language, _ ->
        Localization.getStrings(language)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Localization.getStrings("English"),
    )

    private val _secondTranslationCode = MutableStateFlow("ENG")
    val secondTranslationCode = _secondTranslationCode.asStateFlow()

    private val _appTheme =
        MutableStateFlow(
            if (prefs.getBoolean("is_first_run", true)) {
                AppTheme.SKY_BLUE
            } else {
                try {
                    AppTheme.valueOf(prefs.getString("app_theme", AppTheme.SKY_BLUE.name) ?: AppTheme.SKY_BLUE.name)
                } catch (_: Exception) {
                    AppTheme.SKY_BLUE
                }
            },
        )
    val appTheme = _appTheme.asStateFlow()

    val downloadStatus = languageService.downloadStatus
    val individualProgress = languageService.individualProgress
    val batchProgress = languageService.batchProgress

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _updateStatus = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.Idle)
    val updateStatus = _updateStatus.asStateFlow()

    private val _showDownloadOverlay = MutableStateFlow(value = false)
    val showDownloadOverlay = _showDownloadOverlay.asStateFlow()

    private val checkedTTSLanguages = mutableSetOf<String>()

    init {
        if (prefs.getBoolean("is_first_run", true)) {
            handleFirstRun()
        }

        // Initial DB switch
        viewModelScope.launch {
            dbProvider.switchToTranslation(_selectedTranslationCode.value)
            loadTranslations()
            loadAllBooks()
        }

        // Observe download status to refresh translations list and check for TTS
        viewModelScope.launch {
            var previousStatus = languageService.downloadStatus.value
            languageService.downloadStatus.collect { currentStatus ->
                loadTranslations(triggerRepair = false) // Don't trigger repair in a loop

                // Check for newly downloaded languages to ensure TTS data is also available
                var newlyDownloadedDetected = false
                val prev = previousStatus
                currentStatus.forEach { (lang, status) ->
                    if ((status == LanguageStatus.DOWNLOADED) && (prev[lang] != LanguageStatus.DOWNLOADED)) {
                        checkTTSForLanguage(lang)
                        newlyDownloadedDetected = true

                        // If the newly downloaded language is the one currently selected, switch and reload the reading
                        if (LanguageService.getLanguageCode(lang) == _selectedTranslationCode.value) {
                            dbProvider.switchToTranslation(_selectedTranslationCode.value)
                            val currentD = _currentDate.value
                            if (currentD.isNotEmpty()) {
                                loadReading(currentD)
                            }
                        }
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
        if (checkedTTSLanguages.contains(languageName)) return

        val locale = when (LanguageService.getLanguageCode(languageName)) {
            "ENG" -> Locale.ENGLISH
            "HIN" -> Locale.forLanguageTag("hi-IN")
            "BAN" -> Locale.forLanguageTag("bn-IN")
            "KAN" -> Locale.forLanguageTag("kn-IN")
            "MAL" -> Locale.forLanguageTag("ml-IN")
            "TAM" -> Locale.forLanguageTag("ta-IN")
            "TEL" -> Locale.forLanguageTag("te-IN")
            "MIZO", "MIZ" -> Locale.forLanguageTag("lus-IN")
            "FAR" -> Locale.forLanguageTag("fa-IR")
            else -> null
        }
        locale?.let {
            checkedTTSLanguages.add(languageName)
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
                loadTranslations(triggerRepair = false)
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

    suspend fun getVerseCount(bookId: Int, chapter: Int): Int = repository.getVerseCount(bookId, chapter)

    private fun handleFirstRun() {
        val defaultLanguages = listOf("English", "Malayalam")
        val defaultCodes = listOf("ENG", "MAL")

        // Check if assets are available for default languages
        val allAssetsPresent = defaultLanguages.all { languageService.hasAsset(it) }

        // Start batch download - this is asynchronous
        startBatchDownload(
            defaultLanguages,
            defaultCodes,
            allowNetwork = allAssetsPresent.not(),
            showOverlay = false,
        )

        // Ensure TTS is checked for default languages on first run
        defaultLanguages.forEach { checkTTSForLanguage(it) }

        // Mark first run as complete and ensure defaults are saved in SharedPreferences
        prefs.edit {
            putBoolean("is_first_run", false)
            putString("default_bible", "ENG")
            putString("app_theme", AppTheme.SKY_BLUE.name)
        }
    }

    private fun loadTranslations(triggerRepair: Boolean = true) {
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
                val displayLanguage = if ((it.language == "English-ASV") ||
                    (it.name == "English-ASV")
                ) {
                    "English"
                } else {
                    it.language
                }
                it.copy(name = nativeName, language = displayLanguage)
            }

            val statusMap = languageService.downloadStatus.value
            val downloadedByStatus = allTranslations.filter {
                statusMap[it.language] == LanguageStatus.DOWNLOADED
            }

            _downloadedTranslations.value = (downloadedFromDb + downloadedByStatus).distinctBy { it.code }

            // Ensure TTS for all downloaded translations is checked
            _downloadedTranslations.value.forEach {
                checkTTSForLanguage(it.language)
            }

            // Handle repair logic for existing installations
            if (!prefs.getBoolean("is_first_run", true)) {
                // Check if default translations are actually complete
                val defaultMapping = listOf("English" to "ENG", "Malayalam" to "MAL")
                val incompleteData = mutableListOf<Pair<String, String>>()
                for ((lang, code) in defaultMapping) {
                    if (!repository.isTranslationComplete(code)) {
                        incompleteData.add(lang to code)
                    }
                }

                // Only trigger repair if not already downloading
                val currentStatus = languageService.downloadStatus.value
                val actuallyIncomplete = incompleteData.filter { (lang, _) ->
                    currentStatus[lang] != LanguageStatus.DOWNLOADING
                }

                if (triggerRepair && actuallyIncomplete.isNotEmpty()) {
                    // If they are missing or incomplete after first run, we can try to repair them
                    // (allowing network now if necessary)
                    startBatchDownload(
                        actuallyIncomplete.map { it.first },
                        actuallyIncomplete.map { it.second },
                        force = true,
                        showOverlay = false,
                    )
                } else if (triggerRepair) {
                    // Ensure default languages are eventually marked as downloaded if they weren't finished
                    val missingDefaults = defaultMapping.filter { (lang, _) ->
                        (statusMap[lang] != LanguageStatus.DOWNLOADED) &&
                            (statusMap[lang] != LanguageStatus.DOWNLOADING) &&
                            (statusMap[lang] != LanguageStatus.FAILED)
                    }
                    if (missingDefaults.isNotEmpty()) {
                        startBatchDownload(
                            missingDefaults.map { it.first },
                            missingDefaults.map { it.second },
                            showOverlay = false,
                        )
                    }
                }
            }

            if (triggerRepair) {
                allTranslations.find { it.code == _selectedTranslationCode.value }?.let {
                    if (statusMap[it.language] != LanguageStatus.DOWNLOADED) {
                        languageService.downloadLanguageScript(it.language)
                    }
                }
            }
        }
    }

    fun setTranslation(translationCode: String) {
        val translation = _availableTranslations.value.find { it.code == translationCode }
        translation?.let {
            viewModelScope.launch {
                languageService.downloadLanguageScript(it.language, it.code)
                dbProvider.switchToTranslation(translationCode)
                _selectedTranslationCode.value = translationCode
                _isCurrentTranslationComplete.value = repository.isTranslationComplete(translationCode)
                prefs.edit { putString("default_bible", translationCode) }
                if (_currentDate.value.isNotEmpty()) {
                    loadReading(_currentDate.value)
                }
                loadTranslations(triggerRepair = false)
            }
        }
    }

    fun startBatchDownload(
        languages: List<String>,
        codes: List<String>? = null,
        force: Boolean = false,
        allowNetwork: Boolean = true,
        showOverlay: Boolean = true,
    ) {
        viewModelScope.launch {
            _showDownloadOverlay.value = showOverlay
            languageService.batchDownload(languages, codes, force, allowNetwork)
            _showDownloadOverlay.value = false
            loadTranslations(triggerRepair = false) // Refresh available and downloaded lists after download attempt

            // Automatically ensure TTS/Voice data is installed for the downloaded languages
            languages.forEach { language ->
                checkTTSForLanguage(language)
            }
        }
    }

    fun removeTranslation(language: String, code: String) {
        viewModelScope.launch {
            // Handle edge case: if removing currently selected translation
            if (_selectedTranslationCode.value == code) {
                _selectedTranslationCode.value = "ENG"
                prefs.edit { putString("default_bible", "ENG") }
            }
            if (_secondTranslationCode.value == code) {
                _secondTranslationCode.value = "ENG"
            }

            languageService.removeLanguage(language, code)
            loadTranslations()
            loadAllBooks() // Refresh book list in case a translation was removed

            // Re-load reading with new default if needed
            if (_currentDate.value.isNotEmpty()) {
                loadReading(_currentDate.value)
            }
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

    fun loadChapterVerses(bookId: Int, chapter: Int) {
        viewModelScope.launch {
            _chapterVerses.value = repository.getChapterVerses(bookId, chapter, _selectedTranslationCode.value)
        }
    }

    suspend fun getChapterVerses(bookId: Int, chapter: Int): List<TargetReadingDetails> =
        repository.getChapterVerses(bookId, chapter, _selectedTranslationCode.value)

    fun loadSecondChapterVerses(bookId: Int, chapter: Int, translationCode: String) {
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

    fun resetParallelReading(bookId: Int, chapter: Int) {
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

    fun checkForAppUpdate() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = updateManager.checkForUpdates()) {
                is AppUpdateManager.UpdateResult.NewUpdateAvailable -> {
                    _updateStatus.value = AppUpdateStatus.NewVersionAvailable(result.version, result.downloadUrl)
                    // For now, let's trigger download immediately as per prompt "and update the app"
                    updateManager.downloadAndInstall(result.downloadUrl)
                }

                is AppUpdateManager.UpdateResult.NoUpdateAvailable -> {
                    _updateStatus.value = AppUpdateStatus.UpToDate
                    refreshRemoteTranslations(updateDb = true)
                }

                is AppUpdateManager.UpdateResult.Error -> {
                    _updateStatus.value = AppUpdateStatus.Error(result.message)
                    refreshRemoteTranslations(updateDb = true)
                }
            }
            _isRefreshing.value = false
        }
    }

    fun clearUpdateStatus() {
        _updateStatus.value = AppUpdateStatus.Idle
    }
}

sealed class AppUpdateStatus {
    object Idle : AppUpdateStatus()
    object UpToDate : AppUpdateStatus()
    data class NewVersionAvailable(val version: String, val downloadUrl: String) : AppUpdateStatus()
    data class Error(val message: String) : AppUpdateStatus()
}
