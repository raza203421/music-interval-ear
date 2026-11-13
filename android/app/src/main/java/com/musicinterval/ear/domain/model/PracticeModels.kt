package com.musicinterval.ear.domain.model

import kotlin.math.roundToInt

enum class Direction { Ascending, Descending, Repeated }
enum class Instrument { SoftPiano, PureTone, Flute }
enum class Tempo(val noteMs: Int, val gapMs: Int) { Slow(650, 200), Normal(500, 140), Quick(350, 100) }
data class PracticeSettings(val instrument: Instrument = Instrument.SoftPiano, val tempo: Tempo = Tempo.Normal, val volume: Int = 70, val haptics: Boolean = true, val reduceMotion: Boolean = false)
data class Level(val id: Int, val intervals: List<Int>, val direction: Direction?, val symbol: Int)
data class IntervalTarget(val semitones: Int, val direction: Direction) {
    val key: String get() = "${semitones}_${direction.name}"
    val code: Int get() = if (direction == Direction.Descending) -semitones else semitones
}
data class Question(val target: IntervalTarget, val firstMidi: Int, val secondMidi: Int, val options: List<Int>)
data class Counts(val correct: Int = 0, val attempted: Int = 0) {
    val accuracy: Int get() = if (attempted == 0) 0 else (100.0 * correct / attempted).roundToInt()
    fun add(other: Counts) = Counts(correct + other.correct, attempted + other.attempted)
}
data class Answer(val question: Question, val chosen: Int) {
    val correct: Boolean get() = chosen == question.target.semitones
}
data class DrillSession(val id: String, val seed: Int, val levelId: Int, val settings: PracticeSettings, val question: Question, val remainingBag: List<Int>, val positionUses: List<Int>, val answers: List<Answer> = emptyList(), val previousCode: Int? = null)
data class DrillResult(val id: String, val timestamp: Long, val levelId: Int, val settings: PracticeSettings, val answers: List<Answer>, val remainingBag: List<Int>) {
    val counts: Counts get() = Counts(answers.count { it.correct }, answers.size)
    val missed: Answer? get() = answers.lastOrNull { !it.correct }
}
data class PracticeData(val settings: PracticeSettings = PracticeSettings(), val selectedLevelId: Int = 1, val totals: Counts = Counts(), val completedDrills: Int = 0, val levels: Map<Int, Counts> = emptyMap(), val intervals: Map<String, Counts> = emptyMap(), val bags: Map<Int, List<Int>> = emptyMap(), val history: List<DrillResult> = emptyList(), val loaded: Boolean = false, val readError: Boolean = false, val lastCodes: Map<Int, Int> = emptyMap())
enum class SaveState { Idle, Saving, Saved, Error, Reset }
enum class Playback { Idle, Preparing, First, Gap, Second, Complete, Paused, Muted, Error }
