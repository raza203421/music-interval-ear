package com.musicinterval.ear.core.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.musicinterval.ear.R
import com.musicinterval.ear.core.theme.*

@Composable
fun StudioBackground(dark: Boolean = false, loader: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        val colors = when {
            loader -> listOf(DeepPlum.copy(alpha = 0.96f), PrimaryIndigo.copy(alpha = 0.88f), TextPrimary.copy(alpha = 0.97f))
            dark -> listOf(DeepPlum.copy(alpha = 0.96f), PrimaryIndigo.copy(alpha = 0.87f), TextPrimary.copy(alpha = 0.97f))
            else -> listOf(Background.copy(alpha = 0.97f), BackgroundMid.copy(alpha = 0.96f), Lavender.copy(alpha = 0.94f))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        content()
    }
}

@Composable
fun StudioPage(titleRes: Int, subtitle: String? = null, onBack: (() -> Unit)? = null, dark: Boolean = false, bottomBar: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    AppTheme(dark) {
        StudioBackground(dark) {
            Column(Modifier.safeDrawingPadding().fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(Modifier.widthIn(max = 600.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StudioTopBar(titleRes, subtitle, onBack)
                        content()
                        Spacer(Modifier.height(8.dp))
                    }
                }
                if (bottomBar != null) Surface(Modifier.fillMaxWidth(), color = if (dark) DeepPlum else Background.copy(alpha = 0.98f), shadowElevation = 12.dp) {
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.widthIn(max = 600.dp).fillMaxWidth()) { bottomBar() }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioTopBar(titleRes: Int, subtitle: String?, onBack: (() -> Unit)?) {
    Column(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onBack != null) TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.back)) }
        Text(stringResource(titleRes), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.semantics { heading() })
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StudioCard(modifier: Modifier = Modifier, dark: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = if (dark) DeepPlum.copy(alpha = 0.97f) else SurfaceWhite.copy(alpha = 0.96f), contentColor = if (dark) SurfaceWhite else TextPrimary, border = BorderStroke(1.dp, if (dark) Lavender.copy(alpha = 0.28f) else Border), shadowElevation = 4.dp) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun SectionLabel(res: Int) { Text(stringResource(res), style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo, modifier = Modifier.semantics { heading() }) }

@Composable
fun BodyText(res: Int) { Text(stringResource(res), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
