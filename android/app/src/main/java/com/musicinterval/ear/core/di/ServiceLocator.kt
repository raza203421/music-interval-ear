package com.musicinterval.ear.core.di

import android.content.Context
import com.musicinterval.ear.data.audio.AudioTrackIntervalPlayer
import com.musicinterval.ear.data.local.PreferenceStore
import com.musicinterval.ear.data.repository.PracticeRepositoryImpl
import com.musicinterval.ear.data.sample.SampleData
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.SessionStore
import com.musicinterval.ear.domain.repository.PracticeRepository
import com.musicinterval.ear.domain.usecase.*

object ServiceLocator {
    private var instance: AppViewModelFactory? = null
    val factory: AppViewModelFactory get() = checkNotNull(instance)
    fun initialize(context: Context) {
        if (instance != null) return
        val repository: PracticeRepository = PracticeRepositoryImpl(PreferenceStore(context.applicationContext))
        val audio: IntervalAudioPlayer = AudioTrackIntervalPlayer(context.applicationContext)
        instance = AppViewModelFactory(repository, audio, SessionStore(), CreateDrillUseCase(SampleData.levels), AnswerIntervalUseCase(), GetWeakIntervalsUseCase(), SampleData.levels, SampleData.referencePairs)
    }
}
