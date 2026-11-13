package com.musicinterval.ear.presentation.game

import com.musicinterval.ear.domain.model.*

enum class GamePhase { Initializing, Listening, Ready, Feedback, Paused, AudioError, Finished }
data class GameUiState(val session: DrillSession? = null, val phase: GamePhase = GamePhase.Initializing, val playback: Playback = Playback.Idle, val chosen: Int? = null, val leaveDialog: Boolean = false, val remainingFeedback: Long = 0L)
sealed interface GameEvent {
    data class Result(val id: String) : GameEvent
    data class Feedback(val correct: Boolean, val haptics: Boolean) : GameEvent
    data object Leave : GameEvent
}
