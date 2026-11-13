package com.musicinterval.ear.data.local

import android.content.Context
import com.musicinterval.ear.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PreferenceStore(private val context: Context) {
    private val preferences by lazy { context.getSharedPreferences("music_interval_ear", Context.MODE_PRIVATE) }
    suspend fun read(): PracticeData = withContext(Dispatchers.IO) {
        val raw = preferences.getString("envelope", null) ?: return@withContext PracticeData(loaded = true)
        val json = JSONObject(raw)
        require(json.optInt("version") == 1)
        val settings = decodeSettings(json.optJSONObject("settings") ?: JSONObject())
        try {
            val history = json.optJSONArray("history") ?: JSONArray()
            val results = (0 until history.length()).map { decodeResult(history.getJSONObject(it)) }
            PracticeData(settings, json.optInt("selected", 1).coerceIn(1, 5), decodeCounts(json.optJSONObject("totals")), json.optInt("completed").coerceAtLeast(0), decodeMap(json.optJSONObject("levels")).mapKeys { it.key.toInt() }, decodeMap(json.optJSONObject("intervals")), decodeBags(json.optJSONObject("bags")), results.take(50), true, lastCodes = json.optJSONObject("lastCodes")?.let { codes -> codes.keys().asSequence().mapNotNull { key -> key.toIntOrNull()?.takeIf { it in 1..5 }?.let { id -> id to codes.optInt(key).coerceIn(-12, 12) } }.toMap() }.orEmpty())
        } catch (_: Exception) {
            PracticeData(settings = settings, loaded = true, readError = true)
        }
    }
    suspend fun write(data: PracticeData): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject().put("version", 1).put("settings", encodeSettings(data.settings)).put("selected", data.selectedLevelId).put("totals", encodeCounts(data.totals)).put("completed", data.completedDrills)
            json.put("levels", JSONObject().apply { data.levels.forEach { (id, counts) -> put(id.toString(), encodeCounts(counts)) } })
            json.put("intervals", JSONObject().apply { data.intervals.forEach { (key, counts) -> put(key, encodeCounts(counts)) } })
            json.put("bags", JSONObject().apply { data.bags.forEach { (id, bag) -> put(id.toString(), JSONArray(bag)) } })
            json.put("lastCodes", JSONObject().apply { data.lastCodes.forEach { (id, code) -> put(id.toString(), code) } })
            json.put("history", JSONArray().apply { data.history.take(50).forEach { put(encodeResult(it)) } })
            preferences.edit().putString("envelope", json.toString()).commit()
        }.getOrDefault(false)
    }
    private fun encodeCounts(value: Counts) = JSONObject().put("correct", value.correct).put("attempted", value.attempted)
    private fun decodeCounts(json: JSONObject?): Counts {
        val attempted = json?.optInt("attempted", 0)?.coerceAtLeast(0) ?: 0
        return Counts((json?.optInt("correct", 0) ?: 0).coerceIn(0, attempted), attempted)
    }
    private fun encodeSettings(value: PracticeSettings) = JSONObject().put("instrument", value.instrument.name).put("tempo", value.tempo.name).put("volume", value.volume).put("haptics", value.haptics).put("reduceMotion", value.reduceMotion)
    private fun decodeSettings(json: JSONObject) = PracticeSettings(Instrument.entries.firstOrNull { it.name == json.optString("instrument") } ?: Instrument.SoftPiano, Tempo.entries.firstOrNull { it.name == json.optString("tempo") } ?: Tempo.Normal, json.optInt("volume", 70).coerceIn(20, 100), json.optBoolean("haptics", true), json.optBoolean("reduceMotion", false))
    private fun decodeMap(json: JSONObject?): Map<String, Counts> = json?.keys()?.asSequence()?.associateWith { decodeCounts(json.optJSONObject(it)) }.orEmpty()
    private fun decodeBags(json: JSONObject?): Map<Int, List<Int>> = json?.keys()?.asSequence()?.mapNotNull { key -> key.toIntOrNull()?.takeIf { it in 1..5 }?.let { it to intList(json.optJSONArray(key)).filter { n -> n in -12..12 }.distinct() } }?.toMap().orEmpty()
    private fun intList(array: JSONArray?): List<Int> = if (array == null) emptyList() else (0 until array.length()).map { array.getInt(it) }
    private fun encodeResult(value: DrillResult) = JSONObject().put("id", value.id).put("time", value.timestamp).put("level", value.levelId).put("settings", encodeSettings(value.settings)).put("bag", JSONArray(value.remainingBag)).put("answers", JSONArray().apply {
        value.answers.forEach { answer -> put(JSONObject().put("target", answer.question.target.semitones).put("direction", answer.question.target.direction.name).put("first", answer.question.firstMidi).put("second", answer.question.secondMidi).put("options", JSONArray(answer.question.options)).put("chosen", answer.chosen)) }
    })
    private fun decodeResult(json: JSONObject): DrillResult {
        val array = json.getJSONArray("answers")
        require(array.length() in 1..10)
        val answers = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val distance = item.getInt("target")
            val direction = Direction.valueOf(item.getString("direction"))
            val first = item.getInt("first")
            val second = item.getInt("second")
            val options = intList(item.getJSONArray("options"))
            val chosen = item.getInt("chosen")
            require(distance in 0..12 && first in 60..72 && second in 48..84 && options.size == 4 && options.distinct().size == 4 && distance in options && chosen in options)
            Answer(Question(IntervalTarget(distance, direction), first, second, options), chosen)
        }
        val level = json.getInt("level")
        require(level in 1..5 && answers.dropLast(1).all { it.correct } && (!answers.last().correct || answers.size == 10))
        return DrillResult(json.getString("id"), json.getLong("time"), level, decodeSettings(json.getJSONObject("settings")), answers, intList(json.optJSONArray("bag")))
    }
}
