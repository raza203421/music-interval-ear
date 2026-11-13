package com.musicinterval.ear.presentation.levels

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.*

@Composable
fun LevelsRoute(factory: AppViewModelFactory, onPlay: (Int) -> Unit, onBack: () -> Unit) {
    val vm: LevelsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    CollectEvents(vm.events) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    LevelsScreen(state, vm::select, { onPlay(state.selected) }, onBack, vm::retry)
}

@Composable
fun LevelsScreen(state: LevelsUiState, onSelect: (Int) -> Unit, onPlay: () -> Unit, onBack: () -> Unit, onRetry: () -> Unit) {
    StudioPage(R.string.levels_title, stringResource(R.string.levels_subtitle), onBack, bottomBar = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.play_set, levelLabel(state.selected)), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(R.string.play, onPlay)
        }
    }) {
        state.levels.forEach { level ->
            val selected = state.selected == level.id
            val description = stringResource(if (selected) R.string.selected else R.string.not_selected)
            Surface(Modifier.fillMaxWidth().animateContentSize().selectable(selected, role = Role.RadioButton, onClick = { onSelect(level.id) }).semantics { stateDescription = description }, shape = RoundedCornerShape(18.dp), color = if (selected) BackgroundMid else SurfaceWhite.copy(alpha = 0.96f), border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) PrimaryIndigo else Border), shadowElevation = 4.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(painterResource(symbolRes(level.id)), null, Modifier.size(56.dp), contentScale = ContentScale.Fit)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.set_number, level.id), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                            Text(levelLabel(level.id), style = MaterialTheme.typography.titleMedium)
                        }
                        RadioButton(selected, onClick = null)
                    }
                    Text(stringResource(levelDetailRes(level.id)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    val counts = state.progress[level.id]
                    Text(if (counts == null || counts.attempted == 0) stringResource(R.string.new_set) else stringResource(R.string.progress_counts, counts.accuracy, counts.attempted), style = MaterialTheme.typography.bodyMedium, color = PrimaryIndigo)
                    if (selected || state.recommended == level.id) Text(stringResource(if (selected) R.string.selected else R.string.recommended), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo)
                }
            }
        }
        SaveStatus(state.save, onRetry)
    }
}
