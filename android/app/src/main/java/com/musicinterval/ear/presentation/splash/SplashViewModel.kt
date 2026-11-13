package com.musicinterval.ear.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SplashUiState(val titleReady: Boolean = true, val finished: Boolean = false)
class SplashViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(SplashUiState())
    val uiState = mutableState.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    init {
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            mutableState.update { it.copy(finished = true) }
        }
    }
}
