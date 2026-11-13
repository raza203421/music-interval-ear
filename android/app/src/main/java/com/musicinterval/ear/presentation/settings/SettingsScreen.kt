package com.musicinterval.ear.presentation.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.presentation.dialog.ResetProgressDialog

@Composable
fun SettingsRoute(factory: AppViewModelFactory, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    CollectEvents(vm.events) { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    PlaybackLifecycle(Unit, {}, vm::pause)
    SettingsScreen(state, vm::instrument, vm::tempo, vm::volume, vm::haptics, vm::reduceMotion, vm::preview, vm::pause, vm::retry, vm::requestReset, vm::cancelReset, vm::reset, onBack)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onInstrument: (Instrument) -> Unit, onTempo: (Tempo) -> Unit, onVolume: (Int) -> Unit, onHaptics: (Boolean) -> Unit, onMotion: (Boolean) -> Unit, onPreview: () -> Unit, onStop: () -> Unit, onSave: () -> Unit, onResetRequest: () -> Unit, onResetCancel: () -> Unit, onResetConfirm: () -> Unit, onBack: () -> Unit) {
    StudioPage(R.string.settings_title, stringResource(R.string.settings_subtitle), onBack, bottomBar = {
        PrimaryButton(if (state.save == SaveState.Error) R.string.retry_save else R.string.save, onSave, enabled = !state.loading && state.dirty && state.save != SaveState.Saving)
    }) {
        StudioCard {
            SectionLabel(R.string.instrument)
            Column(Modifier.selectableGroup()) {
                Instrument.entries.forEach { instrument ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(state.settings.instrument == instrument, role = Role.RadioButton, onClick = { onInstrument(instrument) }), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RadioButton(state.settings.instrument == instrument, onClick = null)
                        Text(stringResource(instrumentRes(instrument)), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            val playing = state.playback in listOf(Playback.Preparing, Playback.First, Playback.Gap, Playback.Second)
            SecondaryButton(if (playing) R.string.stop else R.string.listen_sample, if (playing) onStop else onPreview, enabled = !state.loading)
            if (state.playback != Playback.Idle) Text(stringResource(playbackRes(state.playback)), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        StudioCard {
            SectionLabel(R.string.tempo)
            Column(Modifier.selectableGroup()) {
                Tempo.entries.forEach { tempo ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(state.settings.tempo == tempo, role = Role.RadioButton, onClick = { onTempo(tempo) }), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RadioButton(state.settings.tempo == tempo, onClick = null)
                        Text(stringResource(tempoRes(tempo)), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        StudioCard {
            SectionLabel(R.string.app_volume)
            val volume = stringResource(R.string.percent, state.settings.volume)
            Text(volume, style = MaterialTheme.typography.headlineMedium, color = PrimaryIndigo, modifier = Modifier.semantics { stateDescription = volume })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(R.string.decrease, { onVolume(-10) }, Modifier.weight(1f), enabled = state.settings.volume > 20)
                SecondaryButton(R.string.increase, { onVolume(10) }, Modifier.weight(1f), enabled = state.settings.volume < 100)
            }
            BodyText(R.string.volume_hint)
        }
        StudioCard {
            SettingToggle(R.string.haptic_feedback, state.settings.haptics, onHaptics)
            SettingToggle(R.string.reduce_motion, state.settings.reduceMotion, onMotion)
        }
        SaveStatus(state.save, onSave)
        StudioCard { SectionLabel(R.string.local_data); BodyText(R.string.local_data_body) }
        SecondaryButton(R.string.reset_progress, onResetRequest, enabled = state.save != SaveState.Saving)
    }
    if (state.resetDialog) ResetProgressDialog(onResetCancel, onResetConfirm)
}

@Composable
private fun SettingToggle(label: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    val state = stringResource(if (checked) R.string.on_state else R.string.off_state)
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(checked, role = Role.Switch, onValueChange = onChange).semantics { stateDescription = state }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = null)
    }
}
