package com.musicinterval.ear.presentation.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.*
import com.musicinterval.ear.domain.model.Playback
import com.musicinterval.ear.presentation.dialog.LeaveDrillDialog

@Composable
fun GameRoute(factory: AppViewModelFactory, sessionId: String, onResult: (String) -> Unit, onBack: () -> Unit, onMenu: () -> Unit) {
    val vm: GameViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val result by rememberUpdatedState(onResult)
    val back by rememberUpdatedState(onBack)
    val haptic = LocalHapticFeedback.current
    PlaybackLifecycle(sessionId, { vm.open(sessionId) }, vm::pause)
    CollectEvents(vm.events) { event ->
            when (event) {
                is GameEvent.Result -> result(event.id)
                GameEvent.Leave -> back()
                is GameEvent.Feedback -> if (event.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
    }
    GameScreen(state, vm::choose, vm::replay, vm::stop, vm::requestLeave, vm::keepPracticing, vm::leave, vm::viewResults, onMenu)
}

@Composable
fun GameScreen(state: GameUiState, onChoose: (Int) -> Unit, onReplay: () -> Unit, onStop: () -> Unit, onBack: () -> Unit, onKeep: () -> Unit, onLeave: () -> Unit, onResults: () -> Unit, onMenu: () -> Unit) {
    val session = state.session
    StudioPage(R.string.listen_drill, session?.let { levelLabel(it.levelId) }, onBack, dark = true) {
        if (session == null) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            BodyText(R.string.preparing_notes)
        } else if (state.phase == GamePhase.Finished) {
            StudioCard {
                SectionLabel(R.string.drill_complete)
                Text(stringResource(R.string.correct_count, session.answers.count { it.correct }, session.answers.size), style = MaterialTheme.typography.headlineMedium, color = PrimaryIndigo, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                PrimaryButton(R.string.view_results, onResults)
                SecondaryButton(R.string.menu, onMenu)
            }
        } else {
            Text(stringResource(R.string.question_number, (session.answers.size + if (state.chosen == null) 1 else 0).coerceAtMost(10)), style = MaterialTheme.typography.labelLarge, color = GoldLight)
            StudioCard {
                SectionLabel(R.string.listening_activity)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    NoteMarker(R.string.first_note, state.playback == Playback.First)
                    Box(Modifier.width(32.dp).height(1.dp).background(Border))
                    NoteMarker(R.string.second_note, state.playback == Playback.Second)
                }
                Text(stringResource(R.string.listen_instruction), style = MaterialTheme.typography.bodyLarge)
                val status = stringResource(playbackRes(state.playback))
                Text(status, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.semantics { stateDescription = status; liveRegion = LiveRegionMode.Polite })
                if (state.phase == GamePhase.Listening) {
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = PrimaryIndigo)
                    SecondaryButton(R.string.stop, onStop)
                } else if (state.phase != GamePhase.Feedback) {
                    SecondaryButton(if (state.phase == GamePhase.AudioError) R.string.try_again else R.string.replay, onReplay)
                }
            }
            Text(stringResource(R.string.what_interval), style = MaterialTheme.typography.titleLarge, color = SurfaceWhite)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 340.dp && LocalDensity.current.fontScale < 1.25f) 2 else 1
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    session.question.options.chunked(columns).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { option ->
                                val selected = state.chosen == option
                                val selectedDescription = stringResource(if (selected) R.string.answer_selected else R.string.answer_available)
                                val border by animateColorAsState(if (selected) GoldLight else Lavender.copy(alpha = 0.65f), tween(if (LocalReduceMotion.current) 100 else 150), label = "answer")
                                OutlinedButton(onClick = { onChoose(option) }, enabled = state.phase == GamePhase.Ready, shape = RoundedCornerShape(16.dp), border = BorderStroke(if (selected) 2.dp else 1.dp, border), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) PrimaryIndigo else DeepPlum, contentColor = SurfaceWhite, disabledContainerColor = if (selected) PrimaryIndigo else DeepPlum.copy(alpha = 0.6f), disabledContentColor = if (state.phase == GamePhase.Feedback) SurfaceWhite else Lavender.copy(alpha = 0.65f)), modifier = Modifier.weight(1f).heightIn(min = 76.dp).semantics { stateDescription = selectedDescription }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stringResource(answerRes(option)), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                                        Text(stringResource(shortRes(option)), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (state.phase == GamePhase.Feedback) {
                val answer = session.answers.lastOrNull()
                if (answer != null) StudioCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeedbackMark(answer.correct)
                        Text(stringResource(if (answer.correct) R.string.correct_feedback else R.string.wrong_feedback, intervalLabel(answer.chosen), intervalLabel(answer.question.target.semitones)), style = MaterialTheme.typography.titleMedium, color = if (answer.correct) Success else Error, modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    Text(stringResource(R.string.revealed_pair, stringResource(directionRes(answer.question.target.direction)), midiLabel(answer.question.firstMidi), midiLabel(answer.question.secondMidi)), style = MaterialTheme.typography.bodyMedium)
                }
            } else Text(stringResource(R.string.no_time_limit), style = MaterialTheme.typography.bodyMedium, color = Lavender)
        }
    }
    if (state.leaveDialog) LeaveDrillDialog(onKeep, onLeave)
}

@Composable
private fun NoteMarker(labelRes: Int, active: Boolean) {
    val reduced = LocalReduceMotion.current
    val scale by animateFloatAsState(if (active && !reduced) 1.05f else 1f, tween(if (reduced) 100 else 120), label = "note")
    val alpha by animateFloatAsState(if (active) 1f else 0.5f, tween(120), label = "noteLight")
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(64.dp).scale(scale).background(if (active) Lavender else BackgroundMid, CircleShape), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.sprite_1), null, Modifier.size(40.dp).alpha(alpha), contentScale = ContentScale.Fit)
        }
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}
