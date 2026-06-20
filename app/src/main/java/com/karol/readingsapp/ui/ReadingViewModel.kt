package com.karol.readingsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karol.readingsapp.data.ReadingRepository
import com.karol.readingsapp.data.TargetReadingDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReadingViewModel(private val repository: ReadingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Map<String, List<TargetReadingDetails>>>(emptyMap())
    val uiState: StateFlow<Map<String, List<TargetReadingDetails>>> = _uiState

    fun loadReading(date: String) {
        viewModelScope.launch {
            _uiState.value = repository.getReadingsForDate(date)
        }
    }

    fun getVersesForReading(bookName: String, chapter: Int): List<TargetReadingDetails> {
        return _uiState.value.values.asSequence()
            .flatten()
            .filter { (it.bookName == bookName) && (it.chapter == chapter) }
            .toList()
    }
}
