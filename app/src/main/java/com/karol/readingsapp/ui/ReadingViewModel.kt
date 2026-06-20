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

class ReadingViewModel(private val repository: ReadingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState: StateFlow<Map<String, List<TargetReadingDetails>>> = _uiState

    private val _monthlyPlan = MutableStateFlow<Map<String, List<SimpleReading>>>(emptyMap())
    val monthlyPlan = _monthlyPlan.asStateFlow()

    private val _availableTranslations = MutableStateFlow<List<BibleTranslation>>(emptyList())
    val availableTranslations = _availableTranslations.asStateFlow()

    private val _selectedTranslationCode = MutableStateFlow("ENG")
    val selectedTranslationCode = _selectedTranslationCode.asStateFlow()

    private var currentDate = ""
    private var currentMonth = ""

    init {
        loadTranslations()
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            _availableTranslations.value = repository.getAvailableTranslations()
        }
    }

    fun setTranslation(translationCode: String) {
        _selectedTranslationCode.value = translationCode
        if (currentDate.isNotEmpty()) {
            loadReading(currentDate)
        }
        // Monthly plan is translation-independent now as it only shows the schedule
    }

    fun loadReading(date: String) {
        currentDate = date
        viewModelScope.launch {
            _uiState.value = repository.getReadingsForDate(date, _selectedTranslationCode.value)
        }
    }

    fun loadMonthlyPlan(month: String) {
        currentMonth = month
        viewModelScope.launch {
            _monthlyPlan.value = repository.getReadingsForMonth(month)
        }
    }

    fun getVersesForReading(bookName: String, chapter: Int): List<TargetReadingDetails> {
        return _uiState.value.values.asSequence()
            .flatten()
            .filter { (it.bookName == bookName) && (it.chapter == chapter) }
            .toList()
    }
}
