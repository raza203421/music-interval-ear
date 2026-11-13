package com.musicinterval.ear.core.ui

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun PlaybackLifecycle(key: Any?, onResume: () -> Unit, onPause: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val resume by rememberUpdatedState(onResume)
    val pause by rememberUpdatedState(onPause)
    DisposableEffect(owner, key) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) resume()
            if (event == Lifecycle.Event.ON_PAUSE) pause()
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) resume()
        onDispose { owner.lifecycle.removeObserver(observer); pause() }
    }
}

@Composable
fun <T> CollectEvents(events: Flow<T>, onEvent: (T) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val current by rememberUpdatedState(onEvent)
    LaunchedEffect(Unit) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { current(it) }
        }
    }
}
