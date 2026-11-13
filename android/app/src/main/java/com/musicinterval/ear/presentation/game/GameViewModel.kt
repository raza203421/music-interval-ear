package com.musicinterval.ear.presentation.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.core.config.GameConfig
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class GameViewModel(private val player: IntervalAudioPlayer, private val sessions: SessionStore, private val creator: CreateDrillUseCase, private val answerer: AnswerIntervalUseCase) : ViewModel() {
    private val mutableState = MutableStateFlow(GameUiState())
    val uiState = mutableState.asStateFlow()
    private val channel = Channel<GameEvent>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    private val snapshots = linkedMapOf<String, GameUiState>()
    private var audioJob: Job? = null
    private var feedbackJob: Job? = null
    private var deadline = 0L
    private var foreground = false
    fun open(id: String) {
        foreground = true
        if (mutableState.value.session?.id != id) {
            mutableState.value.session?.let { snapshots[it.id] = mutableState.value }
            val session = sessions.session(id)
            mutableState.update { snapshots[id] ?: GameUiState(session = session) }
        }
        val session = mutableState.value.session ?: return
        if (sessions.result(id) != null) {
            mutableState.update { it.copy(phase = GamePhase.Finished, playback = Playback.Idle, leaveDialog = false) }
            return
        }
        if (mutableState.value.phase == GamePhase.Initializing) replay()
        else if (mutableState.value.phase == GamePhase.Feedback && !mutableState.value.leaveDialog) scheduleFeedback()
        sessions.put(session)
    }
    fun replay() {
        val state = mutableState.value
        val session = state.session ?: return
        if (!foreground || state.phase == GamePhase.Finished || state.phase == GamePhase.Feedback || state.leaveDialog) return
        audioJob?.cancel()
        player.stop()
        mutableState.update { it.copy(phase = GamePhase.Listening, playback = Playback.Preparing) }
        audioJob = viewModelScope.launch {
            player.play(session.question.firstMidi, session.question.secondMidi, session.settings) { playback ->
                mutableState.update { current -> current.copy(playback = playback, phase = when (playback) { Playback.Complete -> GamePhase.Ready; Playback.Error -> GamePhase.AudioError; Playback.Muted, Playback.Paused -> GamePhase.Paused; else -> GamePhase.Listening }) }
            }
        }
    }
    fun choose(value: Int) {
        val state = mutableState.value
        val session = state.session ?: return
        if (state.phase != GamePhase.Ready || value !in session.question.options || !foreground) return
        val updated = answerer.answer(session, value)
        val correct = updated.answers.last().correct
        sessions.put(updated)
        mutableState.update { it.copy(session = updated, chosen = value, phase = GamePhase.Feedback, remainingFeedback = if (correct) GameConfig.CORRECT_FEEDBACK_MS else GameConfig.WRONG_FEEDBACK_MS) }
        channel.trySend(GameEvent.Feedback(correct, session.settings.haptics))
        scheduleFeedback()
    }
    private fun scheduleFeedback() {
        if (feedbackJob?.isActive == true || !foreground) return
        deadline = SystemClock.elapsedRealtime() + mutableState.value.remainingFeedback
        feedbackJob = viewModelScope.launch {
            delay(mutableState.value.remainingFeedback)
            val session = mutableState.value.session ?: return@launch
            if (answerer.finished(session)) {
                val result = DrillResult(session.id, System.currentTimeMillis(), session.levelId, session.settings, session.answers, session.remainingBag)
                sessions.finish(result)
                mutableState.update { it.copy(phase = GamePhase.Finished, remainingFeedback = 0L) }
                channel.send(GameEvent.Result(session.id))
            } else {
                val next = creator.next(session)
                sessions.put(next)
                mutableState.update { it.copy(session = next, chosen = null, phase = GamePhase.Paused, remainingFeedback = 0L) }
                feedbackJob = null
                replay()
            }
        }
    }
    fun pause() {
        foreground = false
        if (feedbackJob?.isActive == true) mutableState.update { it.copy(remainingFeedback = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)) }
        feedbackJob?.cancel(); feedbackJob = null
        if (audioJob?.isActive == true) { audioJob?.cancel(); player.stop() }
        audioJob = null
        mutableState.update { if (it.phase in listOf(GamePhase.Ready, GamePhase.Listening, GamePhase.AudioError)) it.copy(phase = GamePhase.Paused, playback = Playback.Paused) else it }
        mutableState.value.session?.let { session -> snapshots[session.id] = mutableState.value }
        while (snapshots.size > 3) snapshots.remove(snapshots.keys.first())
    }
    fun stop() { pause(); foreground = true }
    fun requestLeave() {
        if (mutableState.value.phase == GamePhase.Finished) { channel.trySend(GameEvent.Leave); return }
        pause()
        mutableState.update { it.copy(leaveDialog = true) }
    }
    fun keepPracticing() {
        foreground = true
        mutableState.update { it.copy(leaveDialog = false) }
        if (mutableState.value.phase == GamePhase.Feedback) scheduleFeedback()
    }
    fun leave() { pause(); mutableState.update { it.copy(leaveDialog = false) }; channel.trySend(GameEvent.Leave) }
    fun viewResults() { mutableState.value.session?.id?.let { channel.trySend(GameEvent.Result(it)) } }
    override fun onCleared() { pause() }
}
