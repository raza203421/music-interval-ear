package com.musicinterval.ear.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.musicinterval.ear.R
import com.musicinterval.ear.domain.model.*

fun intervalRes(value: Int): Int = when (value) { 0 -> R.string.interval_0; 1 -> R.string.interval_1; 2 -> R.string.interval_2; 3 -> R.string.interval_3; 4 -> R.string.interval_4; 5 -> R.string.interval_5; 6 -> R.string.interval_6; 7 -> R.string.interval_7; 8 -> R.string.interval_8; 9 -> R.string.interval_9; 10 -> R.string.interval_10; 11 -> R.string.interval_11; else -> R.string.interval_12 }
fun answerRes(value: Int): Int = when (value) { 0 -> R.string.answer_0; 1 -> R.string.answer_1; 2 -> R.string.answer_2; 3 -> R.string.answer_3; 4 -> R.string.answer_4; 5 -> R.string.answer_5; 6 -> R.string.answer_6; 7 -> R.string.answer_7; 8 -> R.string.answer_8; 9 -> R.string.answer_9; 10 -> R.string.answer_10; 11 -> R.string.answer_11; else -> R.string.answer_12 }
fun shortRes(value: Int): Int = when (value) { 0 -> R.string.short_0; 1 -> R.string.short_1; 2 -> R.string.short_2; 3 -> R.string.short_3; 4 -> R.string.short_4; 5 -> R.string.short_5; 6 -> R.string.short_6; 7 -> R.string.short_7; 8 -> R.string.short_8; 9 -> R.string.short_9; 10 -> R.string.short_10; 11 -> R.string.short_11; else -> R.string.short_12 }
fun cueRes(value: Int): Int = when (value) { 0 -> R.string.cue_0; 1 -> R.string.cue_1; 2 -> R.string.cue_2; 3 -> R.string.cue_3; 4 -> R.string.cue_4; 5 -> R.string.cue_5; 6 -> R.string.cue_6; 7 -> R.string.cue_7; 8 -> R.string.cue_8; 9 -> R.string.cue_9; 10 -> R.string.cue_10; 11 -> R.string.cue_11; else -> R.string.cue_12 }
fun descendingCueRes(value: Int): Int = when (value) { 0 -> R.string.cue_0; 1 -> R.string.down_1; 2 -> R.string.down_2; 3 -> R.string.down_3; 4 -> R.string.down_4; 5 -> R.string.down_5; 6 -> R.string.down_6; 7 -> R.string.down_7; 8 -> R.string.down_8; 9 -> R.string.down_9; 10 -> R.string.down_10; 11 -> R.string.down_11; else -> R.string.down_12 }
fun levelRes(id: Int): Int = when (id) { 1 -> R.string.level_1; 2 -> R.string.level_2; 3 -> R.string.level_3; 4 -> R.string.level_4; else -> R.string.level_5 }
fun levelDetailRes(id: Int): Int = when (id) { 1 -> R.string.level_detail_1; 2 -> R.string.level_detail_2; 3 -> R.string.level_detail_3; 4 -> R.string.level_detail_4; else -> R.string.level_detail_5 }
fun symbolRes(id: Int): Int = when (id) { 1 -> R.drawable.sprite_1; 2 -> R.drawable.sprite_2; 3 -> R.drawable.sprite_3; 4 -> R.drawable.sprite_4; else -> R.drawable.sprite_5 }
fun directionRes(value: Direction): Int = when (value) { Direction.Ascending -> R.string.ascending; Direction.Descending -> R.string.descending; Direction.Repeated -> R.string.repeated }
fun playbackRes(value: Playback): Int = when (value) { Playback.Preparing -> R.string.preparing_notes; Playback.First -> R.string.playing_first; Playback.Gap -> R.string.between_notes; Playback.Second -> R.string.playing_second; Playback.Complete -> R.string.ready_answer; Playback.Error -> R.string.audio_error; Playback.Muted -> R.string.media_muted; Playback.Paused -> R.string.resume_question; else -> R.string.listen_instruction }
fun instrumentRes(value: Instrument): Int = when (value) { Instrument.SoftPiano -> R.string.soft_piano; Instrument.PureTone -> R.string.pure_tone; Instrument.Flute -> R.string.flute }
fun tempoRes(value: Tempo): Int = when (value) { Tempo.Slow -> R.string.slow; Tempo.Normal -> R.string.normal; Tempo.Quick -> R.string.quick }
@Composable
fun intervalLabel(value: Int): String = stringResource(intervalRes(value))
@Composable
fun levelLabel(value: Int): String = stringResource(levelRes(value))
@Composable
fun midiLabel(value: Int): String {
    val nameRes = when (value % 12) { 0 -> R.string.note_c; 1 -> R.string.note_cs; 2 -> R.string.note_d; 3 -> R.string.note_ef; 4 -> R.string.note_e; 5 -> R.string.note_f; 6 -> R.string.note_fs; 7 -> R.string.note_g; 8 -> R.string.note_af; 9 -> R.string.note_a; 10 -> R.string.note_bf; else -> R.string.note_b }
    return stringResource(R.string.note_octave, stringResource(nameRes), value / 12 - 1)
}
