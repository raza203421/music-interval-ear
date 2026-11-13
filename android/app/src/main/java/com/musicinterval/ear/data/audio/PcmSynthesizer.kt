package com.musicinterval.ear.data.audio

import com.musicinterval.ear.domain.model.*
import kotlin.math.*

object PcmSynthesizer {
    const val SAMPLE_RATE = 44100
    fun note(midi: Int, settings: PracticeSettings): ShortArray {
        val duration = settings.tempo.noteMs / 1000.0
        val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
        val sampleCount = settings.tempo.noteMs * SAMPLE_RATE / 1000
        return ShortArray(sampleCount) { index ->
            val time = index.toDouble() / SAMPLE_RATE
            val phase = 2 * PI * frequency * time
            val envelope = when {
                index >= sampleCount - SAMPLE_RATE / 100 -> 0.0
                time < 0.015 -> time / 0.015
                time < 0.075 -> 1.0 - 0.3 * (time - 0.015) / 0.060
                time > duration - 0.010 -> 0.0
                time > duration - 0.110 -> 0.7 * (duration - 0.010 - time) / 0.100
                else -> 0.7
            }
            val wave = when (settings.instrument) {
                Instrument.PureTone -> sin(phase)
                Instrument.SoftPiano -> (sin(phase) + 0.35 * sin(2 * phase) + 0.15 * sin(3 * phase)) / 1.5 * exp(-3 * time)
                Instrument.Flute -> (sin(phase) + 0.12 * sin(2 * phase) + 0.06 * sin(3 * phase)) / 1.18
            }
            (wave * envelope.coerceIn(0.0, 1.0) * 0.65 * (settings.volume / 100.0) * Short.MAX_VALUE).roundToInt().toShort()
        }
    }
}
