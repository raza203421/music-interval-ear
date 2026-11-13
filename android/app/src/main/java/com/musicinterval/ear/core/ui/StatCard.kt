package com.musicinterval.ear.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import com.musicinterval.ear.core.theme.LocalReduceMotion
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.musicinterval.ear.core.theme.PrimaryIndigo
import com.musicinterval.ear.R
import com.musicinterval.ear.domain.model.SaveState

@Composable
fun StatCard(titleRes: Int, value: String, modifier: Modifier = Modifier) {
    StudioCard(modifier.semantics(mergeDescendants = true) { stateDescription = value }) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = PrimaryIndigo)
        Text(stringResource(titleRes), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SaveStatus(state: SaveState, onRetry: () -> Unit) {
    if (state != SaveState.Idle) {
        Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state == SaveState.Saving) LinearProgressIndicator(Modifier.fillMaxWidth())
            AnimatedVisibility(visible = state == SaveState.Saved, enter = if (LocalReduceMotion.current) fadeIn(tween(100)) else fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.8f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackMark(true)
                    Text(stringResource(R.string.saved), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (state != SaveState.Saved) Text(stringResource(when (state) { SaveState.Saving -> R.string.saving; SaveState.Saved -> R.string.saved; SaveState.Error -> R.string.save_error; SaveState.Reset -> R.string.progress_reset; else -> R.string.saved }), color = if (state == SaveState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            if (state == SaveState.Error) SecondaryButton(R.string.retry_save, onRetry)
        }
    }
}
