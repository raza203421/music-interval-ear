package com.musicinterval.ear.presentation.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LevelsUiState(val levels: List<Level> = emptyList(), val selected: Int = 1, val recommended: Int = 1, val progress: Map<Int, Counts> = emptyMap(), val save: SaveState = SaveState.Idle)
class LevelsViewModel(private val repository: PracticeRepository, levels: List<Level>) : ViewModel() {
    private val mutableState = MutableStateFlow(LevelsUiState(levels = levels))
    val uiState = mutableState.asStateFlow()
    private val channel = Channel<Unit>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private var saveJob: Job? = null
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.data.collect { data ->
            val counts = data.levels[data.selectedLevelId] ?: Counts()
            mutableState.update { it.copy(selected = if (it.save == SaveState.Error || it.save == SaveState.Saving) it.selected else data.selectedLevelId, progress = data.levels, recommended = if (counts.attempted >= 20 && counts.accuracy >= 80) (data.selectedLevelId + 1).coerceAtMost(5) else data.selectedLevelId) }
        } }
    }
    fun select(id: Int) {
        mutableState.update { it.copy(selected = id, save = SaveState.Saving) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val success = repository.selectLevel(id)
            mutableState.update { it.copy(save = if (success) SaveState.Saved else SaveState.Error) }
            if (success && repository.data.value.settings.haptics) channel.send(Unit)
        }
    }
    fun retry() = select(mutableState.value.selected)
}
