package com.musicinterval.ear.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data object Levels : Screen
    data class Game(val sessionId: String) : Screen
    data class GameOver(val sessionId: String) : Screen
    data class ReferenceSongs(val focusInterval: Int? = null) : Screen
    data object Stats : Screen
    data object Settings : Screen
}
