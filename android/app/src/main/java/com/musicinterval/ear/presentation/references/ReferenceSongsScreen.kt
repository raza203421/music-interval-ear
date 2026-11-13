package com.musicinterval.ear.presentation.references

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
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
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.*
import com.musicinterval.ear.domain.model.*

@Composable
fun ReferenceSongsRoute(factory: AppViewModelFactory, focus: Int?, onBack: () -> Unit) {
    val vm: ReferenceSongsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    PlaybackLifecycle(focus, { vm.focus(focus) }, vm::pause)
    ReferenceSongsScreen(state, vm::filter, vm::expand, vm::play, vm::pause, onBack)
}

@Composable
fun ReferenceSongsScreen(state: ReferenceSongsUiState, onFilter: (Direction?) -> Unit, onExpand: (Int) -> Unit, onPlay: (Int, Direction) -> Unit, onStop: () -> Unit, onBack: () -> Unit) {
    StudioPage(R.string.reference_songs_title, stringResource(R.string.references_subtitle), onBack) {
        StudioCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.sprite_3), null, Modifier.size(72.dp), contentScale = ContentScale.Fit)
                Text(stringResource(R.string.reference_intro), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null, Direction.Ascending, Direction.Descending).forEach { filter ->
                val label = if (filter == null) R.string.all else if (filter == Direction.Ascending) R.string.ascending_button else R.string.descending_button
                val selectedText = stringResource(if (state.filter == filter) R.string.selected else R.string.not_selected)
                OutlinedButton(onClick = { onFilter(filter) }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if (state.filter == filter) BackgroundMid else SurfaceWhite), modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { selected = state.filter == filter; stateDescription = selectedText }) { Text(stringResource(label), style = MaterialTheme.typography.labelSmall) }
            }
        }
        val rows = state.references.filter { state.filter == null || it.target.semitones != 0 }.sortedBy { if (it.target.semitones == state.expanded) -1 else it.target.semitones }
        rows.forEach { reference ->
            val value = reference.target.semitones
            val expanded = state.expanded == value
            val expansion = stringResource(if (expanded) R.string.expanded else R.string.collapsed)
            StudioCard(Modifier.animateContentSize(tween(if (LocalReduceMotion.current) 100 else 180))) {
                Column(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { onExpand(value) }.semantics { stateDescription = expansion }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(intervalLabel(value), style = MaterialTheme.typography.titleMedium, color = PrimaryIndigo)
                    Text(pluralStringResource(R.plurals.semitones, value, value), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(stringResource(if (expanded) R.string.hide_reference else R.string.show_reference), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                }
                if (expanded) {
                    val directions = if (value == 0) listOf(Direction.Repeated) else state.filter?.let { listOf(it) } ?: listOf(Direction.Ascending, Direction.Descending)
                    directions.forEach { direction ->
                        HorizontalDivider(color = Border)
                        Text(stringResource(directionRes(direction)), style = MaterialTheme.typography.labelLarge, color = PrimaryIndigo)
                        Text(stringResource(if (direction == Direction.Descending) descendingCueRes(value) else cueRes(value)), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.note_pair, midiLabel(60), midiLabel(60 + if (direction == Direction.Descending) -value else value)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        val playing = state.playingInterval == value && state.playback in listOf(Playback.Preparing, Playback.First, Playback.Gap, Playback.Second)
                        SecondaryButton(if (playing) R.string.stop else R.string.play_example, if (playing) onStop else ({ onPlay(value, direction) }))
                    }
                    if (state.playingInterval == value && state.playback != Playback.Idle) Text(stringResource(playbackRes(state.playback)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                } else Text(stringResource(if (state.filter == Direction.Descending) descendingCueRes(value) else cueRes(value)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
