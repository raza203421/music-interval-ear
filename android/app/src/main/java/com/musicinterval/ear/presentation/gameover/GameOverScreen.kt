package com.musicinterval.ear.presentation.gameover

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.*
import com.musicinterval.ear.domain.model.Playback
import kotlin.math.*

@Composable
fun GameOverRoute(factory: AppViewModelFactory, id: String, onAgain: (Int) -> Unit, onMenu: () -> Unit, onReview: (Int) -> Unit, onLevels: () -> Unit, onBack: () -> Unit) {
    val vm: GameOverViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    PlaybackLifecycle(id, { vm.open(id) }, vm::pause)
    GameOverScreen(state, { state.result?.let { vm.pause(); onAgain(it.levelId) } }, { vm.pause(); onMenu() }, vm::listen, vm::pause, { state.result?.missed?.let { vm.pause(); onReview(it.question.target.semitones) } }, onLevels, vm::retry, onBack)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onAgain: () -> Unit, onMenu: () -> Unit, onListen: () -> Unit, onStop: () -> Unit, onReview: () -> Unit, onLevels: () -> Unit, onRetry: () -> Unit, onBack: () -> Unit) {
    val result = state.result
    StudioPage(R.string.your_results, onBack = onBack, bottomBar = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.results_hint), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            PrimaryButton(R.string.play_again, onAgain, enabled = result != null)
            SecondaryButton(R.string.menu, onMenu)
        }
    }) {
        if (result == null) BodyText(R.string.result_unavailable)
        else {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.sprite_5), null, Modifier.size(112.dp), contentScale = ContentScale.Fit)
                if (result.counts.correct == 10 && !LocalReduceMotion.current) ResultSparkles(result.id)
            }
            StudioCard(dark = true) {
                Text(stringResource(if (result.counts.correct == 10) R.string.ten_in_tune else R.string.good_listening), style = MaterialTheme.typography.titleLarge, color = Lavender)
                val score = stringResource(R.string.percent, result.counts.accuracy)
                Text(score, style = MaterialTheme.typography.displaySmall, color = SurfaceWhite, modifier = Modifier.semantics { stateDescription = score; liveRegion = LiveRegionMode.Polite })
                Text(stringResource(R.string.correct_count, result.counts.correct, result.counts.attempted), style = MaterialTheme.typography.bodyLarge, color = Background)
                Text(levelLabel(result.levelId), style = MaterialTheme.typography.labelLarge, color = GoldLight)
            }
            if (result.counts.correct > 0 && result.counts.attempted > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(R.string.correct_answers, stringResource(R.string.number, result.counts.correct), Modifier.weight(1f))
                StatCard(R.string.questions_heard, stringResource(R.string.number, result.counts.attempted), Modifier.weight(1f))
            }
            val missed = result.missed
            if (missed != null) StudioCard {
                SectionLabel(R.string.your_next_lesson)
                Text(intervalLabel(missed.question.target.semitones), style = MaterialTheme.typography.headlineMedium, color = PrimaryIndigo)
                Text(stringResource(R.string.you_chose, intervalLabel(missed.chosen)), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.revealed_pair, stringResource(directionRes(missed.question.target.direction)), midiLabel(missed.question.firstMidi), midiLabel(missed.question.secondMidi)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(stringResource(if (missed.question.target.direction == com.musicinterval.ear.domain.model.Direction.Descending) descendingCueRes(missed.question.target.semitones) else cueRes(missed.question.target.semitones)), style = MaterialTheme.typography.bodyLarge)
                AudioReviewActions(state.playback, onListen, onStop)
                SecondaryButton(R.string.review_interval, onReview)
            } else StudioCard {
                BodyText(R.string.perfect_drill_body)
                AudioReviewActions(state.playback, onListen, onStop)
            }
            SaveStatus(state.save, onRetry)
            SecondaryButton(R.string.levels, onLevels)
        }
    }
}

@Composable
private fun AudioReviewActions(playback: Playback, onListen: () -> Unit, onStop: () -> Unit) {
    val playing = playback in listOf(Playback.Preparing, Playback.First, Playback.Gap, Playback.Second)
    SecondaryButton(if (playing) R.string.stop else R.string.listen_again, if (playing) onStop else onListen)
    if (playback != Playback.Idle) Text(stringResource(playbackRes(playback)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
private fun ResultSparkles(id: String) {
    val initial = remember(id) { MutableTransitionState(false).apply { targetState = true } }
    val transition = rememberTransition(initial, label = "success")
    val progress by transition.animateFloat(transitionSpec = { tween(450) }, label = "sparkles") { if (it) 1f else 0f }
    Canvas(Modifier.size(180.dp)) {
        if (progress < 1f) repeat(6) { index ->
            val angle = index * PI / 3
            val radius = size.minDimension * (0.2f + progress * 0.3f)
            drawCircle(Gold.copy(alpha = 1f - progress), 3.dp.toPx(), Offset(center.x + cos(angle).toFloat() * radius, center.y + sin(angle).toFloat() * radius))
        }
    }
}
