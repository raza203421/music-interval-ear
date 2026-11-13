package com.musicinterval.ear.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.core.navigation.Screen
import com.musicinterval.ear.domain.model.SessionStore
import com.musicinterval.ear.domain.repository.PracticeRepository
import com.musicinterval.ear.domain.usecase.CreateDrillUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppUiState(val starting: Boolean = false, val reduceMotion: Boolean = false, val backwards: Boolean = false)
class AppViewModel(private val repository: PracticeRepository, private val creator: CreateDrillUseCase, private val sessions: SessionStore) : ViewModel() {
    private val mutableScreen = MutableStateFlow<Screen>(Screen.Splash)
    val screen: StateFlow<Screen> = mutableScreen.asStateFlow()
    private val mutableState = MutableStateFlow(AppUiState())
    val uiState = mutableState.asStateFlow()
    private val stack = mutableListOf<Screen>()
    val backStack: List<Screen> get() = stack.toList()
    private val eventChannel = Channel<Unit>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.data.collect { data -> mutableState.update { it.copy(reduceMotion = data.settings.reduceMotion) } } }
    }
    fun finishSplash() { if (mutableScreen.value == Screen.Splash) { mutableScreen.value = Screen.Menu; stack.clear() } }
    fun navigate(destination: Screen) {
        if (destination == mutableScreen.value) return
        if (mutableScreen.value != Screen.Splash) stack.add(mutableScreen.value)
        mutableState.update { it.copy(backwards = false) }
        mutableScreen.value = destination
        prune()
    }
    fun menu() { stack.clear(); mutableState.update { it.copy(backwards = true) }; mutableScreen.value = Screen.Menu }
    fun back() {
        if (mutableScreen.value == Screen.Menu || mutableScreen.value == Screen.Splash) { eventChannel.trySend(Unit); return }
        mutableState.update { it.copy(backwards = true) }
        mutableScreen.value = if (stack.isEmpty()) Screen.Menu else stack.removeAt(stack.lastIndex)
    }
    fun start(levelId: Int? = null) {
        if (mutableState.value.starting || mutableScreen.value is Screen.Game) return
        mutableState.update { it.copy(starting = true) }
        viewModelScope.launch {
            repository.load()
            val session = creator.create(repository.data.value, levelId ?: repository.data.value.selectedLevelId)
            sessions.put(session)
            navigate(Screen.Game(session.id))
            mutableState.update { it.copy(starting = false) }
        }
    }
    private fun prune() {
        val ids = (stack + mutableScreen.value).mapNotNull { when (it) { is Screen.Game -> it.sessionId; is Screen.GameOver -> it.sessionId; else -> null } }.distinct().takeLast(3)
        stack.removeAll { when (it) { is Screen.Game -> it.sessionId !in ids; is Screen.GameOver -> it.sessionId !in ids; else -> false } }
    }
}
