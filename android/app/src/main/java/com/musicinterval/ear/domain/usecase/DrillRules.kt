package com.musicinterval.ear.domain.usecase

import com.musicinterval.ear.domain.model.*
import java.util.UUID
import kotlin.random.Random

class CreateDrillUseCase(private val levels: List<Level>) {
    fun create(data: PracticeData, levelId: Int = data.selectedLevelId): DrillSession {
        val initial = DrillSession(UUID.randomUUID().toString(), Random.nextInt(), levelId, data.settings, Question(IntervalTarget(4, Direction.Ascending), 60, 64, emptyList()), data.bags[levelId].orEmpty(), List(4) { 0 }, previousCode = data.lastCodes[levelId])
        return next(initial)
    }
    fun next(session: DrillSession): DrillSession {
        val level = levels.first { it.id == session.levelId }
        val random = Random(session.seed + session.answers.size * 7919)
        val eligible = level.intervals.flatMap { value ->
            when {
                value == 0 -> listOf(0)
                level.direction == null -> listOf(value, -value)
                level.direction == Direction.Descending -> listOf(-value)
                else -> listOf(value)
            }
        }
        var bag = session.remainingBag.filter { it in eligible }
        if (bag.isEmpty()) {
            bag = eligible.shuffled(random)
            val last = session.answers.lastOrNull()?.question?.target?.code ?: session.previousCode
            if (bag.size > 1 && bag.first() == last) bag = bag.drop(1) + bag.first()
        }
        val code = bag.first()
        val distance = kotlin.math.abs(code)
        val target = IntervalTarget(distance, when { code < 0 -> Direction.Descending; code == 0 -> Direction.Repeated; else -> Direction.Ascending })
        val distractors = level.intervals.filter { it != distance }.shuffled(random).sortedBy { kotlin.math.abs(it - distance) }
        val nearest = distractors.take(2)
        val third = distractors.drop(2).random(random)
        val least = session.positionUses.minOrNull() ?: 0
        val position = session.positionUses.indices.filter { session.positionUses[it] == least }.random(random)
        val choices = (nearest + third).shuffled(random).toMutableList().apply { add(position, distance) }
        val root = random.nextInt(60, 73)
        return session.copy(question = Question(target, root, root + code, choices), remainingBag = bag.drop(1), positionUses = session.positionUses.mapIndexed { index, value -> value + if (index == position) 1 else 0 })
    }
}
class AnswerIntervalUseCase {
    fun answer(session: DrillSession, chosen: Int): DrillSession = session.copy(answers = session.answers + Answer(session.question, chosen))
    fun finished(session: DrillSession): Boolean = session.answers.lastOrNull()?.correct == false || session.answers.size >= 10
}
class GetWeakIntervalsUseCase {
    fun ranked(data: PracticeData): List<Pair<IntervalTarget, Counts>> = data.intervals.mapNotNull { (key, counts) ->
        val parts = key.split('_')
        val value = parts.firstOrNull()?.toIntOrNull()
        val direction = Direction.entries.firstOrNull { it.name == parts.getOrNull(1) }
        if (value == null || direction == null) null else IntervalTarget(value, direction) to counts
    }.filter { it.second.attempted >= 3 && it.second.accuracy < 80 }
        .sortedWith(compareBy<Pair<IntervalTarget, Counts>> { it.second.correct.toDouble() / it.second.attempted }.thenByDescending { it.second.attempted }.thenBy { it.first.semitones }).take(3)
    fun trainingLevel(target: IntervalTarget): Int = when {
        target.direction == Direction.Repeated -> 5
        target.direction == Direction.Descending -> 4
        target.semitones in listOf(4, 5, 7, 12) -> 1
        target.semitones in listOf(1, 2, 3) -> 2
        else -> 3
    }
}
