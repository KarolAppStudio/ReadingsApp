package com.karol.readingsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.data.BibleTranslation
import com.karol.readingsapp.data.ReadingRepository
import com.karol.readingsapp.data.SimpleReading
import com.karol.readingsapp.data.TargetReadingDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.karol.readingsapp.data.LanguageService
import kotlinx.coroutines.flow.asStateFlow

class ReadingViewModel(
    private val repository: ReadingRepository,
    private val languageService: LanguageService,
) : ViewModel() {
    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState: StateFlow<Map<String, List<TargetReadingDetails>>> = _uiState

    private val _monthlyPlan = MutableStateFlow<Map<String, List<SimpleReading>>>(emptyMap())
    val monthlyPlan = _monthlyPlan.asStateFlow()

    private val _availableTranslations = MutableStateFlow<List<BibleTranslation>>(emptyList())
    val availableTranslations = _availableTranslations.asStateFlow()

    private val _selectedTranslationCode = MutableStateFlow("ENG")
    val selectedTranslationCode = _selectedTranslationCode.asStateFlow()

    val downloadStatus = languageService.downloadStatus

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private var currentMonth = ""

    init {
        loadTranslations()
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            val translations = repository.getAvailableTranslations()
            _availableTranslations.value = translations
            translations.forEach { 
                android.util.Log.d("ReadingViewModel", "Translation: code=${it.code}, language=${it.language}, name=${it.name}")
            }
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

    fun loadMonthlyPlan(month: String) {
        currentMonth = month
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
