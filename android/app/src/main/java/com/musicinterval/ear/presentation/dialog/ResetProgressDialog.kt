package com.musicinterval.ear.presentation.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.musicinterval.ear.R

@Composable
fun ResetProgressDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onCancel, title = { Text(stringResource(R.string.reset_title)) }, text = { Text(stringResource(R.string.reset_body)) }, confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.reset)) } }, dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) } })
}
