package com.karol.readingsapp.ui

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.data.LanguageService
import com.karol.readingsapp.data.ReadingRepository
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.data.bible.ChapterReference
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.data.plan.SimpleReading
import com.karol.readingsapp.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReadingViewModel(
    private val repository: ReadingRepository,
    private val languageService: LanguageService,
    context: Context,
) : ViewModel() {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState: StateFlow<Map<String, List<TargetReadingDetails>>> = _uiState

    private val _monthlyPlan = MutableStateFlow<Map<String, List<SimpleReading>>>(emptyMap())
    val monthlyPlan = _monthlyPlan.asStateFlow()

    private val _availableTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val availableTranslations = _availableTranslations.asStateFlow()

    private val _allBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    val allBooks = _allBooks.asStateFlow()

    private val _allChapters = MutableStateFlow<List<ChapterReference>>(emptyList())
    val allChapters = _allChapters.asStateFlow()

    private val _chapterVerses = MutableStateFlow<List<TargetReadingDetails>>(emptyList())
    val chapterVerses = _chapterVerses.asStateFlow()

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
    val batchProgress = languageService.batchProgress

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        loadTranslations()
        loadAllBooks()
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
            val translations =
                repository.getAvailableTranslations().map {
                    val lang = it.language.lowercase()
                    val nativeName =
                        when {
                            (lang.contains("hindi") || (lang == "hi") || (lang == "hin")) -> "हिन्दी"
                            (lang.contains("bangla") || lang.contains("bengali") || (lang == "bn") || (lang == "ban")) -> "বাংলা"
                            (lang.contains("kannada") || (lang == "kn") || (lang == "kan")) -> "ಕನ್ನಡ"
                            (lang.contains("malayalam") || (lang == "ml") || (lang == "mal")) -> "മലയാളം"
                            (lang.contains("tamil") || (lang == "ta") || (lang == "tam")) -> "தமிழ்"
                            (lang.contains("telugu") || (lang == "te") || (lang == "tel")) -> "తెలుగు"
                            it.name == "English-ASV" -> "English"
                            else -> it.name.removeSuffix(" Bible")
                        }
                    val displayLanguage = if (it.language == "English-ASV") "English" else it.language
                    it.copy(name = nativeName, language = displayLanguage)
                }
            _availableTranslations.value = translations

            val isFirstRun = prefs.getBoolean("is_first_run", true)
            if (isFirstRun) {
                val allLanguages = translations.asSequence().map { it.language }.distinct().toList()
                startBatchDownload(allLanguages)
                setTheme(AppTheme.SKY_BLUE)
                prefs.edit { putBoolean("is_first_run", false) }
            }

            translations.find { it.code == _selectedTranslationCode.value }?.let {
                languageService.downloadLanguageScript(it.language)
            }
        }
    }

    fun setTranslation(translationCode: String) {
        val translation = _availableTranslations.value.find { it.code == translationCode }
        translation?.let {
            viewModelScope.launch {
                languageService.downloadLanguageScript(it.language)
                _selectedTranslationCode.value = translationCode
                prefs.edit { putString("default_bible", translationCode) }
                if (_currentDate.value.isNotEmpty()) {
                    loadReading(_currentDate.value)
                }
            }
        }
    }

    fun startBatchDownload(languages: List<String>) {
        viewModelScope.launch {
            languageService.batchDownload(languages)
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
}
