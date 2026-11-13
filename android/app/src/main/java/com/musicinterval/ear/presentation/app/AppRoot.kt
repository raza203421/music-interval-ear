package com.musicinterval.ear.presentation.app

import android.animation.ValueAnimator
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musicinterval.ear.core.ui.CollectEvents
import com.musicinterval.ear.core.di.ServiceLocator
import com.musicinterval.ear.core.navigation.Screen
import com.musicinterval.ear.core.theme.LocalReduceMotion
import com.musicinterval.ear.presentation.splash.SplashRoute
import com.musicinterval.ear.presentation.menu.MenuRoute
import com.musicinterval.ear.presentation.levels.LevelsRoute
import com.musicinterval.ear.presentation.game.GameRoute
import com.musicinterval.ear.presentation.game.GameViewModel
import com.musicinterval.ear.presentation.gameover.GameOverRoute
import com.musicinterval.ear.presentation.references.ReferenceSongsRoute
import com.musicinterval.ear.presentation.stats.StatsRoute
import com.musicinterval.ear.presentation.settings.SettingsRoute

@Composable
fun AppRoot() {
    val factory = ServiceLocator.factory
    val vm: AppViewModel = viewModel(factory = factory)
    val game: GameViewModel = viewModel(factory = factory)
    val screen by vm.screen.collectAsStateWithLifecycle()
    val state by vm.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val reduced = state.reduceMotion || !ValueAnimator.areAnimatorsEnabled()
    val shift = with(LocalDensity.current) { 12.dp.roundToPx() }
    SideEffect {
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            val light = screen != Screen.Splash && screen !is Screen.Game
            controller.isAppearanceLightStatusBars = light
            controller.isAppearanceLightNavigationBars = light
        }
    }
    CollectEvents(vm.events) { activity?.finish() }
    BackHandler {
        if (screen is Screen.Game) game.requestLeave() else vm.back()
    }
    CompositionLocalProvider(LocalReduceMotion provides reduced) {
        AnimatedContent(targetState = screen, transitionSpec = {
            val duration = when { reduced -> 100; initialState == Screen.Splash -> 400; targetState is Screen.GameOver -> 200; else -> 180 }
            if (reduced || initialState == Screen.Splash || targetState is Screen.GameOver) fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
            else (fadeIn(tween(duration)) + slideInVertically(tween(duration)) { if (state.backwards) -shift else shift }) togetherWith fadeOut(tween(duration))
        }, label = "destination") { destination ->
            when (destination) {
                Screen.Splash -> SplashRoute(factory, vm::finishSplash)
                Screen.Menu -> MenuRoute(factory, state.starting, { vm.start() }, { vm.navigate(Screen.Levels) }, { vm.navigate(Screen.ReferenceSongs()) }, { vm.navigate(Screen.Stats) }, { vm.navigate(Screen.Settings) })
                Screen.Levels -> LevelsRoute(factory, vm::start, vm::back)
                is Screen.Game -> GameRoute(factory, destination.sessionId, { id -> if (vm.screen.value is Screen.Game) vm.navigate(Screen.GameOver(id)) }, vm::back, { game.pause(); vm.menu() })
                is Screen.GameOver -> GameOverRoute(factory, destination.sessionId, vm::start, vm::menu, { vm.navigate(Screen.ReferenceSongs(it)) }, { vm.navigate(Screen.Levels) }, vm::back)
                is Screen.ReferenceSongs -> ReferenceSongsRoute(factory, destination.focusInterval, vm::back)
                Screen.Stats -> StatsRoute(factory, { vm.start() }, { vm.navigate(Screen.Levels) }, vm::back)
                Screen.Settings -> SettingsRoute(factory, vm::back)
            }
        }
    }
}
