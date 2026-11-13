package com.musicinterval.ear.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class GameOverUiState(val result: DrillResult? = null, val save: SaveState = SaveState.Idle, val playback: Playback = Playback.Idle)
class GameOverViewModel(private val repository: PracticeRepository, private val sessions: SessionStore, private val player: IntervalAudioPlayer) : ViewModel() {
    private val mutableState = MutableStateFlow(GameOverUiState())
    val uiState = mutableState.asStateFlow()
    private var audioJob: Job? = null
    private var saveJob: Job? = null
    fun open(id: String) {
        if (mutableState.value.result?.id == id) return
        val result = sessions.result(id) ?: repository.data.value.history.firstOrNull { it.id == id }
        mutableState.update { GameOverUiState(result = result) }
        retry()
    }
    fun retry() {
        val result = mutableState.value.result ?: return
        if (mutableState.value.save == SaveState.Saving) return
        mutableState.update { it.copy(save = SaveState.Saving) }
        saveJob = viewModelScope.launch {
            val success = repository.complete(result)
            if (mutableState.value.result?.id == result.id) mutableState.update { it.copy(save = if (success) SaveState.Saved else SaveState.Error) }
        }
    }
    fun listen() {
        val result = mutableState.value.result ?: return
        val question = result.missed?.question ?: result.answers.last().question
        pause()
        audioJob = viewModelScope.launch { player.play(question.firstMidi, question.secondMidi, result.settings) { status -> mutableState.update { it.copy(playback = status) } } }
    }
    fun pause() { if (audioJob?.isActive == true) { audioJob?.cancel(); player.stop() }; audioJob = null; mutableState.update { it.copy(playback = Playback.Idle) } }
    override fun onCleared() { pause() }
}
