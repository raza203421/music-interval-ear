package com.musicinterval.ear.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

data class SettingsUiState(val settings: PracticeSettings = PracticeSettings(), val dirty: Boolean = false, val save: SaveState = SaveState.Idle, val resetDialog: Boolean = false, val playback: Playback = Playback.Idle, val loading: Boolean = true)
class SettingsViewModel(private val repository: PracticeRepository, private val player: IntervalAudioPlayer) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val uiState = mutableState.asStateFlow()
    private var audioJob: Job? = null
    private var writeJob: Job? = null
    private var retryReset = false
    private val channel = Channel<Unit>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.data.collect { data -> mutableState.update { it.copy(settings = if (it.dirty) it.settings else data.settings, loading = !data.loaded) } } }
    }
    private fun edit(transform: (PracticeSettings) -> PracticeSettings) {
        pause()
        mutableState.update { val next = transform(it.settings); it.copy(settings = next, dirty = next != repository.data.value.settings, save = SaveState.Idle) }
    }
    fun instrument(value: Instrument) = edit { it.copy(instrument = value) }
    fun tempo(value: Tempo) = edit { it.copy(tempo = value) }
    fun volume(delta: Int) = edit { it.copy(volume = (it.volume + delta).coerceIn(20, 100)) }
    fun haptics(value: Boolean) = edit { it.copy(haptics = value) }
    fun reduceMotion(value: Boolean) = edit { it.copy(reduceMotion = value) }
    fun preview() {
        pause()
        val settings = mutableState.value.settings
        audioJob = viewModelScope.launch { player.play(60, 67, settings) { status -> mutableState.update { it.copy(playback = status) } } }
    }
    fun pause() {
        if (audioJob?.isActive == true) { audioJob?.cancel(); player.stop() }
        audioJob = null
        mutableState.update { it.copy(playback = Playback.Idle) }
    }
    fun retry() { if (retryReset) reset() else save() }
    fun save() {
        retryReset = false
        if (writeJob?.isActive == true) return
        pause()
        val settings = mutableState.value.settings
        mutableState.update { it.copy(save = SaveState.Saving) }
        writeJob = viewModelScope.launch {
            val success = repository.saveSettings(settings)
            mutableState.update { it.copy(save = if (success) SaveState.Saved else SaveState.Error, dirty = if (success) it.settings != settings else true) }
            if (success && settings.haptics) channel.send(Unit)
        }
    }
    fun requestReset() { mutableState.update { it.copy(resetDialog = true) } }
    fun cancelReset() { mutableState.update { it.copy(resetDialog = false) } }
    fun reset() {
        retryReset = true
        if (writeJob?.isActive == true) return
        mutableState.update { it.copy(resetDialog = false, save = SaveState.Saving) }
        writeJob = viewModelScope.launch {
            val success = repository.reset()
            mutableState.update { it.copy(save = if (success) SaveState.Reset else SaveState.Error) }
        }
    }
    override fun onCleared() { pause() }
}
