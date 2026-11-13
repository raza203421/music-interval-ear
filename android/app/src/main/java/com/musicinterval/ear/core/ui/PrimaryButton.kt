package com.musicinterval.ear.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.musicinterval.ear.core.theme.*

@Composable
fun PrimaryButton(labelRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && !LocalReduceMotion.current) 0.98f else 1f, spring(dampingRatio = 0.80f, stiffness = 500f), label = "press")
    val shape = RoundedCornerShape(18.dp)
    Button(onClick = onClick, enabled = enabled, interactionSource = interaction, shape = shape, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent, contentColor = SurfaceWhite, disabledContentColor = DisabledText), contentPadding = PaddingValues(0.dp), modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).scale(scale).shadow(if (enabled) 6.dp else 0.dp, shape).background(if (enabled) Brush.verticalGradient(listOf(AccentRed, AccentRedEnd)) else Brush.verticalGradient(listOf(BackgroundMid, BackgroundMid)), shape)) {
        Text(stringResource(labelRes), Modifier.padding(horizontal = 20.dp, vertical = 18.dp), style = MaterialTheme.typography.labelLarge)
    }
}
