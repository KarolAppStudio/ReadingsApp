package com.karol.readingsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.data.LanguageService
import com.karol.readingsapp.data.ReadingRepository
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.data.plan.SimpleReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReadingViewModel(
    private val repository: ReadingRepository,
    private val languageService: LanguageService,
) : ViewModel() {
    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState: StateFlow<Map<String, List<TargetReadingDetails>>> = _uiState

    private val _monthlyPlan = MutableStateFlow<Map<String, List<SimpleReading>>>(emptyMap())
    val monthlyPlan = _monthlyPlan.asStateFlow()

    private val _availableTranslations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val availableTranslations = _availableTranslations.asStateFlow()

    private val _allBooks = MutableStateFlow<List<BookEntity>>(emptyList())
    val allBooks = _allBooks.asStateFlow()

    private val _chapterVerses = MutableStateFlow<List<TargetReadingDetails>>(emptyList())
    val chapterVerses = _chapterVerses.asStateFlow()

    private val _selectedTranslationCode = MutableStateFlow("ENG")
    val selectedTranslationCode = _selectedTranslationCode.asStateFlow()

    val downloadStatus = languageService.downloadStatus

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        loadTranslations()
        loadAllBooks()
    }

    private fun loadAllBooks() {
        viewModelScope.launch {
            _allBooks.value = repository.getAllBooks()
        }
    }

    suspend fun getChapterCount(bookId: Int): Int {
        return repository.getChapterCount(bookId)
    }

    suspend fun getVerseCount(bookId: Int, chapter: Int): Int {
        return repository.getVerseCount(bookId, chapter)
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            val translations = repository.getAvailableTranslations()
            _availableTranslations.value = translations
        }
    }

    fun setTranslation(translationCode: String) {
        val translation = _availableTranslations.value.find { it.code == translationCode }
        translation?.let {
            viewModelScope.launch {
                languageService.downloadLanguageScript(it.language)
                _selectedTranslationCode.value = translationCode
                if (_currentDate.value.isNotEmpty()) {
                    loadReading(_currentDate.value)
                }
            }
        }
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

    fun loadMonthlyPlan(month: String) {
        viewModelScope.launch {
            _monthlyPlan.value = repository.getReadingsForMonth(month)
        }
    }

    fun getVersesByBookId(bookId: Int, chapter: Int): List<TargetReadingDetails> {
        return _uiState.value.values.asSequence()
            .flatten()
            .filter { (it.bookId == bookId) && (it.chapter == chapter) }
            .toList()
    }
}
