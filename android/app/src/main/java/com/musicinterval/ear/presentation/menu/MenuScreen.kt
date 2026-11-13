package com.musicinterval.ear.presentation.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.*

@Composable
fun MenuRoute(factory: AppViewModelFactory, starting: Boolean, onPlay: () -> Unit, onLevels: () -> Unit, onReferences: () -> Unit, onStats: () -> Unit, onSettings: () -> Unit) {
    val vm: MenuViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    MenuScreen(state, starting, onPlay, onLevels, onReferences, onStats, onSettings, vm::retry)
}

@Composable
fun MenuScreen(state: MenuUiState, starting: Boolean, onPlay: () -> Unit, onLevels: () -> Unit, onReferences: () -> Unit, onStats: () -> Unit, onSettings: () -> Unit, onRetry: () -> Unit) {
    StudioPage(R.string.app_name, stringResource(R.string.your_practice_studio), bottomBar = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.drill_rule), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            PrimaryButton(R.string.play, onPlay, enabled = !state.loading && !starting)
        }
    }) {
        StudioCard(dark = true) {
            Row(Modifier.fillMaxWidth().heightIn(min = 142.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.hero_title), style = MaterialTheme.typography.headlineMedium, color = SurfaceWhite)
                    Text(stringResource(R.string.hero_subtitle), style = MaterialTheme.typography.bodyMedium, color = Lavender)
                }
                Image(painterResource(R.drawable.logo), null, Modifier.size(100.dp), contentScale = ContentScale.Fit)
            }
        }
        StudioCard {
            SectionLabel(R.string.selected_set)
            Text(levelLabel(state.data.selectedLevelId), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(levelDetailRes(state.data.selectedLevelId)), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            SecondaryButton(R.string.levels, onLevels)
        }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.data.readError) {
            BodyText(R.string.read_error)
            SecondaryButton(R.string.retry, onRetry)
        }
        if (state.data.totals.accuracy > 0 && state.data.completedDrills > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(R.string.lifetime_accuracy, stringResource(R.string.percent, state.data.totals.accuracy), Modifier.weight(1f))
                StatCard(R.string.drills_completed, stringResource(R.string.number, state.data.completedDrills), Modifier.weight(1f))
            }
        } else StudioCard {
            SectionLabel(R.string.start_listening)
            BodyText(R.string.first_drill)
        }
        if (state.recommendedLevel != state.data.selectedLevelId) Text(stringResource(R.string.recommended_level, levelLabel(state.recommendedLevel)), style = MaterialTheme.typography.bodyMedium, color = PrimaryIndigo)
        BodyText(R.string.menu_hint)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(R.string.reference_songs, onReferences, Modifier.weight(1f))
            SecondaryButton(R.string.stats, onStats, Modifier.weight(1f))
        }
        SecondaryButton(R.string.settings, onSettings)
    }
}
