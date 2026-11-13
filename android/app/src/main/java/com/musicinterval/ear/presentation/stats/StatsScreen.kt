package com.musicinterval.ear.presentation.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.presentation.levels.LevelsViewModel
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.*
import com.musicinterval.ear.domain.model.*
import java.text.DateFormat
import java.util.Date

@Composable
fun StatsRoute(factory: AppViewModelFactory, onPlay: () -> Unit, onLevels: () -> Unit, onBack: () -> Unit) {
    val vm: StatsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val levelsVm: LevelsViewModel = viewModel(factory = factory)
    CollectEvents(vm.events) { id -> levelsVm.select(id); onLevels() }
    StatsScreen(state, onPlay, onLevels, vm::train, onBack, vm::retry)
}

@Composable
fun StatsScreen(state: StatsUiState, onPlay: () -> Unit, onLevels: () -> Unit, onTrain: (IntervalTarget) -> Unit, onBack: () -> Unit, onRetry: () -> Unit) {
    StudioPage(R.string.stats_title, stringResource(R.string.stats_subtitle), onBack) {
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.data.readError) { BodyText(R.string.read_error); SecondaryButton(R.string.retry, onRetry) }
        if (state.data.completedDrills == 0) {
            StudioCard {
                Image(painterResource(R.drawable.sprite_4), null, Modifier.size(96.dp).align(Alignment.CenterHorizontally), contentScale = ContentScale.Fit)
                Text(stringResource(R.string.stats_empty_title), style = MaterialTheme.typography.headlineMedium, color = PrimaryIndigo)
                BodyText(R.string.stats_empty_body)
            }
            PrimaryButton(R.string.play, onPlay, enabled = !state.loading)
        } else {
            if (state.data.totals.accuracy > 0) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(R.string.lifetime_accuracy, stringResource(R.string.percent, state.data.totals.accuracy), Modifier.weight(1f))
                StatCard(R.string.drills_completed, stringResource(R.string.number, state.data.completedDrills), Modifier.weight(1f))
            } else Text(stringResource(R.string.correct_count, state.data.totals.correct, state.data.totals.attempted), style = MaterialTheme.typography.titleLarge)
            SectionLabel(R.string.weak_intervals)
            if (state.weak.isEmpty()) {
                StudioCard { BodyText(if (state.intervalRows.any { it.second.attempted >= 3 }) R.string.strong_intervals else R.string.explore_intervals) }
            }
            state.weak.forEach { (target, counts) ->
                StudioCard {
                    IntervalAccuracy(target, counts)
                    SecondaryButton(R.string.train_this_set, { onTrain(target) })
                }
            }
            SectionLabel(R.string.interval_accuracy)
            state.intervalRows.forEach { (target, counts) -> StudioCard { IntervalAccuracy(target, counts) } }
            SectionLabel(R.string.recent_drills)
            state.data.history.take(10).forEach { result ->
                StudioCard {
                    Text(levelLabel(result.levelId), style = MaterialTheme.typography.titleMedium, color = PrimaryIndigo)
                    Text(stringResource(R.string.recent_summary, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(result.timestamp)), result.counts.correct, result.counts.attempted), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            SecondaryButton(R.string.levels, onLevels)
        }
    }
}

@Composable
private fun IntervalAccuracy(target: IntervalTarget, counts: Counts) {
    val label = stringResource(R.string.interval_direction, intervalLabel(target.semitones), stringResource(directionRes(target.direction)))
    val spoken = stringResource(R.string.accuracy_description, label, counts.correct, counts.attempted, counts.accuracy)
    val fraction by animateFloatAsState(counts.correct.toFloat() / counts.attempted.coerceAtLeast(1), tween(if (LocalReduceMotion.current) 100 else 180), label = "accuracy")
    Column(Modifier.semantics(mergeDescendants = true) { stateDescription = spoken }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = PrimaryIndigo)
        Text(stringResource(R.string.correct_count_percent, counts.correct, counts.attempted, counts.accuracy), style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(6.dp), color = PrimaryIndigo, trackColor = BackgroundMid)
    }
}
