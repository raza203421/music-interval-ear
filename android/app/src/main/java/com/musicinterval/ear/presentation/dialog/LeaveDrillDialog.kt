package com.musicinterval.ear.presentation.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.musicinterval.ear.R

@Composable
fun LeaveDrillDialog(onKeepPracticing: () -> Unit, onLeave: () -> Unit) {
    AlertDialog(onDismissRequest = onKeepPracticing, title = { Text(stringResource(R.string.leave_title)) }, text = { Text(stringResource(R.string.leave_body)) }, confirmButton = { TextButton(onClick = onLeave) { Text(stringResource(R.string.leave)) } }, dismissButton = { TextButton(onClick = onKeepPracticing) { Text(stringResource(R.string.keep_practicing)) } })
}
