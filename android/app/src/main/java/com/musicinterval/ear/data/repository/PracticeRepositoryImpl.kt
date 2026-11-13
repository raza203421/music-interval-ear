package com.musicinterval.ear.data.repository

import com.musicinterval.ear.data.local.PreferenceStore
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PracticeRepositoryImpl(private val store: PreferenceStore) : PracticeRepository {
    private val mutableData = MutableStateFlow(PracticeData())
    override val data = mutableData.asStateFlow()
    private val mutex = Mutex()
    override suspend fun load(retry: Boolean) = mutex.withLock {
        if (!mutableData.value.loaded || retry) {
            mutableData.value = runCatching { store.read() }.getOrElse { PracticeData(loaded = true, readError = true) }
        }
    }
    private suspend fun mutate(change: (PracticeData) -> PracticeData): Boolean = mutex.withLock {
        val next = change(mutableData.value).copy(loaded = true, readError = false)
        if (store.write(next)) { mutableData.value = next; true } else false
    }
    override suspend fun selectLevel(id: Int) = mutate { it.copy(selectedLevelId = id.coerceIn(1, 5)) }
    override suspend fun saveSettings(settings: PracticeSettings) = mutate { it.copy(settings = settings) }
    override suspend fun complete(result: DrillResult) = mutate { current ->
        if (current.history.any { it.id == result.id }) current else {
            val intervals = current.intervals.toMutableMap()
            result.answers.forEach { answer ->
                val key = answer.question.target.key
                intervals[key] = (intervals[key] ?: Counts()).add(Counts(if (answer.correct) 1 else 0, 1))
            }
            current.copy(totals = current.totals.add(result.counts), completedDrills = current.completedDrills + 1, levels = current.levels + (result.levelId to (current.levels[result.levelId] ?: Counts()).add(result.counts)), intervals = intervals, bags = current.bags + (result.levelId to result.remainingBag), history = (listOf(result) + current.history).take(50), lastCodes = current.lastCodes + (result.levelId to result.answers.last().question.target.code))
        }
    }
    override suspend fun reset() = mutate { PracticeData(settings = it.settings, loaded = true) }
}
