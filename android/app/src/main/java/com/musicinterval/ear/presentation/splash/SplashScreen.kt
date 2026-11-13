package com.musicinterval.ear.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.R
import com.musicinterval.ear.core.di.AppViewModelFactory
import com.musicinterval.ear.core.theme.*
import com.musicinterval.ear.core.ui.StudioBackground

@Composable
fun SplashRoute(factory: AppViewModelFactory, onFinished: () -> Unit) {
    val vm: SplashViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val finished by vm.finished.collectAsStateWithLifecycle()
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(finished) { if (finished) finish() }
    SplashScreen(state)
}

@Composable
fun SplashScreen(state: SplashUiState) {
    val motion = rememberInfiniteTransition(label = "studio")
    val float by motion.animateFloat(-3f, 3f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "emblem")
    val halo by motion.animateFloat(0.08f, 0.16f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "halo")
    val reduced = LocalReduceMotion.current
    AppTheme(dark = true) {
        StudioBackground(dark = true, loader = true) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 30.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.studio_label), style = MaterialTheme.typography.labelSmall, color = GoldLight)
                Spacer(Modifier.height(42.dp))
                Box(Modifier.size(210.dp).background(Brush.radialGradient(listOf(Gold.copy(alpha = if (reduced) 0.1f else halo), Gold.copy(alpha = 0f)))), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.logo), null, Modifier.size(168.dp).offset(y = (if (reduced) 0f else float).dp), contentScale = ContentScale.Fit)
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = SurfaceWhite, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.splash_subtitle), style = MaterialTheme.typography.bodyLarge, color = Background, textAlign = TextAlign.Center)
                Spacer(Modifier.height(36.dp))
                LinearProgressIndicator(modifier = Modifier.width(144.dp).height(4.dp), color = Gold, trackColor = SurfaceWhite.copy(alpha = 0.24f))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(if (state.finished) R.string.practice_ready else R.string.preparing_practice), style = MaterialTheme.typography.bodyMedium, color = Lavender, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                Spacer(Modifier.height(56.dp))
                Text(stringResource(R.string.offline_studio), style = MaterialTheme.typography.labelSmall, color = Lavender)
            }
        }
    }
}
