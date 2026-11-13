package com.musicinterval.ear.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.musicinterval.ear.R
import androidx.compose.ui.unit.dp

@Composable
fun SecondaryButton(labelRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline), colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).shadow(2.dp, RoundedCornerShape(14.dp))) {
        if (labelRes == R.string.menu) {
            Image(painterResource(R.drawable.icon_home), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(8.dp))
        }
        if (labelRes == R.string.stop) {
            Image(painterResource(R.drawable.icon_pause), null, Modifier.size(24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(8.dp))
        }
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelLarge)
    }
}
