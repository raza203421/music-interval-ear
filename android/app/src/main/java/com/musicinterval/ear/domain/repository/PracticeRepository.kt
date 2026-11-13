package com.musicinterval.ear.domain.repository

import com.musicinterval.ear.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface PracticeRepository {
    val data: StateFlow<PracticeData>
    suspend fun load(retry: Boolean = false)
    suspend fun selectLevel(id: Int): Boolean
    suspend fun saveSettings(settings: PracticeSettings): Boolean
    suspend fun complete(result: DrillResult): Boolean
    suspend fun reset(): Boolean
}
