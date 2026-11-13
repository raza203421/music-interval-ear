package com.musicinterval.ear.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicinterval.ear.domain.model.*
import com.musicinterval.ear.domain.repository.PracticeRepository
import com.musicinterval.ear.domain.usecase.GetWeakIntervalsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatsUiState(val data: PracticeData = PracticeData(), val weak: List<Pair<IntervalTarget, Counts>> = emptyList(), val loading: Boolean = true, val intervalRows: List<Pair<IntervalTarget, Counts>> = emptyList())
class StatsViewModel(private val repository: PracticeRepository, private val weakIntervals: GetWeakIntervalsUseCase) : ViewModel() {
    private val mutableState = MutableStateFlow(StatsUiState())
    val uiState = mutableState.asStateFlow()
    private val channel = Channel<Int>(Channel.BUFFERED)
    val events = channel.receiveAsFlow()
    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch { repository.data.collect { data -> mutableState.update { it.copy(data = data, weak = weakIntervals.ranked(data), loading = !data.loaded, intervalRows = data.intervals.mapNotNull { (key, counts) ->
            val parts = key.split('_')
            val number = parts.firstOrNull()?.toIntOrNull()
            val direction = Direction.entries.firstOrNull { it.name == parts.getOrNull(1) }
            if (number == null || direction == null) null else IntervalTarget(number, direction) to counts
        }.sortedWith(compareBy<Pair<IntervalTarget, Counts>> { it.first.semitones }.thenBy { it.first.direction.ordinal })) } } }
    }
    fun train(target: IntervalTarget) { channel.trySend(weakIntervals.trainingLevel(target)) }
    fun retry() { viewModelScope.launch { repository.load(true) } }
}
