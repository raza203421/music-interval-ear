package com.musicinterval.ear.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import com.musicinterval.ear.domain.usecase.*
import com.musicinterval.ear.presentation.app.AppViewModel
import com.musicinterval.ear.presentation.splash.SplashViewModel
import com.musicinterval.ear.presentation.menu.MenuViewModel
import com.musicinterval.ear.presentation.levels.LevelsViewModel
import com.musicinterval.ear.presentation.game.GameViewModel
import com.musicinterval.ear.presentation.gameover.GameOverViewModel
import com.musicinterval.ear.presentation.references.ReferenceSongsViewModel
import com.musicinterval.ear.presentation.stats.StatsViewModel
import com.musicinterval.ear.presentation.settings.SettingsViewModel

class AppViewModelFactory(private val repository: PracticeRepository, private val audio: IntervalAudioPlayer, private val sessions: SessionStore, private val creator: CreateDrillUseCase, private val answerer: AnswerIntervalUseCase, private val weak: GetWeakIntervalsUseCase, private val levels: List<Level>, private val references: List<Question>) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel(repository, creator, sessions)
        SplashViewModel::class.java -> SplashViewModel()
        MenuViewModel::class.java -> MenuViewModel(repository)
        LevelsViewModel::class.java -> LevelsViewModel(repository, levels)
        GameViewModel::class.java -> GameViewModel(audio, sessions, creator, answerer)
        GameOverViewModel::class.java -> GameOverViewModel(repository, sessions, audio)
        ReferenceSongsViewModel::class.java -> ReferenceSongsViewModel(repository, audio, references)
        StatsViewModel::class.java -> StatsViewModel(repository, weak)
        SettingsViewModel::class.java -> SettingsViewModel(repository, audio)
        else -> throw IllegalArgumentException(modelClass.name)
    } as T
}
