package com.musicinterval.ear.domain.audio

import com.musicinterval.ear.domain.model.*

interface IntervalAudioPlayer {
    suspend fun play(firstMidi: Int, secondMidi: Int, settings: PracticeSettings, onState: (Playback) -> Unit)
    fun stop()
}
