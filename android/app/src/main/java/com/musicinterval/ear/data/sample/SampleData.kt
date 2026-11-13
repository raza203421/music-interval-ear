package com.musicinterval.ear.data.sample

import com.musicinterval.ear.domain.model.*

object SampleData {
    val levels = listOf(
        Level(1, listOf(4, 5, 7, 12), Direction.Ascending, 1),
        Level(2, listOf(1, 2, 3, 4, 5, 7, 12), Direction.Ascending, 2),
        Level(3, (1..12).toList(), Direction.Ascending, 3),
        Level(4, (1..12).toList(), Direction.Descending, 4),
        Level(5, (0..12).toList(), null, 5)
    )
    val referencePairs = (0..12).map { semitones -> Question(IntervalTarget(semitones, if (semitones == 0) Direction.Repeated else Direction.Ascending), 60, 60 + semitones, emptyList()) }
}
