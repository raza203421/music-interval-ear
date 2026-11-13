package com.musicinterval.ear.presentation.references

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ReferenceSongsUiState(val filter: Direction? = null, val expanded: Int? = null, val references: List<Question> = emptyList(), val playback: Playback = Playback.Idle, val playingInterval: Int? = null)
class ReferenceSongsViewModel(private val repository: PracticeRepository, private val player: IntervalAudioPlayer, references: List<Question>) : ViewModel() {
    private val mutableState = MutableStateFlow(ReferenceSongsUiState(references = references))
    val uiState = mutableState.asStateFlow()
    private var job: Job? = null
    private var lastFocus: Int? = null
    fun focus(value: Int?) { if (value != null && value != lastFocus) { lastFocus = value; pause(); mutableState.update { it.copy(expanded = value, filter = null) } } }
    fun filter(value: Direction?) { pause(); mutableState.update { it.copy(filter = value) } }
    fun expand(value: Int) { pause(); mutableState.update { it.copy(expanded = if (it.expanded == value) null else value) } }
    fun play(value: Int, direction: Direction) {
        pause()
        mutableState.update { it.copy(playingInterval = value) }
        job = viewModelScope.launch {
            repository.load()
            player.play(60, 60 + if (direction == Direction.Descending) -value else value, repository.data.value.settings) { status -> mutableState.update { it.copy(playback = status) } }
        }
    }
    fun pause() {
        if (job?.isActive == true) { job?.cancel(); player.stop() }
        job = null
        mutableState.update { it.copy(playback = Playback.Idle, playingInterval = null) }
    }
    override fun onCleared() { pause() }
}
