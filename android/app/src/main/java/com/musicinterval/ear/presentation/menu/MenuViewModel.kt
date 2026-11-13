package com.musicinterval.ear.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MenuUiState(val loading: Boolean = true, val data: PracticeData = PracticeData(), val recommendedLevel: Int = 1)
class MenuViewModel(private val repository: PracticeRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MenuUiState())
    val uiState = mutableState.asStateFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.data.collect { data ->
            val counts = data.levels[data.selectedLevelId] ?: Counts()
            mutableState.update { it.copy(loading = !data.loaded, data = data, recommendedLevel = if (counts.attempted >= 20 && counts.accuracy >= 80) (data.selectedLevelId + 1).coerceAtMost(5) else data.selectedLevelId) }
        } }
    }
    fun retry() { viewModelScope.launch { repository.load(true) } }
}
