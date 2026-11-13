package com.musicinterval.ear.data.audio

import android.content.*
import android.media.*
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.musicinterval.ear.domain.audio.IntervalAudioPlayer
import com.musicinterval.ear.domain.model.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

class AudioTrackIntervalPlayer(private val context: Context) : IntervalAudioPlayer {
    private val manager = context.getSystemService(AudioManager::class.java)
    private var track: AudioTrack? = null
    private var owner: Job? = null
    private var stateCallback: ((Playback) -> Unit)? = null
    private var focus: AudioFocusRequest? = null
    private val lock = Any()
    private val noisy = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) stop() }
    }
    private val devices = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) { if (removedDevices.isNotEmpty()) stop() }
    }
    init {
        ContextCompat.registerReceiver(context, noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_NOT_EXPORTED)
        manager.registerAudioDeviceCallback(devices, Handler(Looper.getMainLooper()))
    }
    override suspend fun play(firstMidi: Int, secondMidi: Int, settings: PracticeSettings, onState: (Playback) -> Unit) {
        stop()
        val job = coroutineContext[Job]
        owner = job
        stateCallback = onState
        try {
            if (manager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) { onState(Playback.Muted); return }
            onState(Playback.Preparing)
            val samples = withContext(Dispatchers.Default) { PcmSynthesizer.note(firstMidi, settings) to PcmSynthesizer.note(secondMidi, settings) }
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes()).setOnAudioFocusChangeListener({ change -> if (change <= 0) stop() }, Handler(Looper.getMainLooper())).build()
            focus = request
            if (manager.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { onState(Playback.Paused); return }
            withContext(Dispatchers.IO) {
                playNote(samples.first, settings.tempo.noteMs, Playback.First, onState)
                onState(Playback.Gap)
                delay(settings.tempo.gapMs.toLong())
                playNote(samples.second, settings.tempo.noteMs, Playback.Second, onState)
            }
            onState(Playback.Complete)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            onState(Playback.Error)
        } finally {
            if (owner === job) { stateCallback = null; release(); owner = null }
        }
    }
    private suspend fun playNote(samples: ShortArray, duration: Int, state: Playback, onState: (Playback) -> Unit) {
        val playbackContext = coroutineContext
        playbackContext.ensureActive()
        val sound = AudioTrack.Builder().setAudioAttributes(attributes()).setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(PcmSynthesizer.SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(samples.size * 2).build()
        synchronized(lock) {
            track = sound
            check(sound.state == AudioTrack.STATE_INITIALIZED)
            check(sound.write(samples, 0, samples.size) == samples.size)
            playbackContext.ensureActive()
            sound.play()
        }
        onState(state)
        delay(duration.toLong())
        synchronized(lock) { if (track === sound) { runCatching { sound.stop() }; sound.release(); track = null } }
    }
    private fun attributes() = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private fun release() {
        synchronized(lock) { track?.let { runCatching { it.pause(); it.flush(); it.stop(); it.release() } }; track = null }
        focus?.let { manager.abandonAudioFocusRequest(it) }
        focus = null
    }
    override fun stop() {
        val callback = stateCallback
        stateCallback = null
        callback?.invoke(Playback.Paused)
        owner?.cancel()
        owner = null
        release()
    }
}
