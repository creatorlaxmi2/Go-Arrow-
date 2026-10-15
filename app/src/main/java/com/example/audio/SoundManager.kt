package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    @Volatile
    var isSoundEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                resumeBgm()
            } else {
                pauseBgm()
            }
        }

    private var tapTrack: AudioTrack? = null
    private var successTrack: AudioTrack? = null
    private var blockedTrack: AudioTrack? = null
    private var undoTrack: AudioTrack? = null
    private var completeTrack: AudioTrack? = null
    private var bgmTrack: AudioTrack? = null

    init {
        Thread {
            try {
                // Initialize pure in-memory PCM AudioTracks.
                // AudioTrack streams raw PCM directly to AudioFlinger without invoking MediaCodec
                // or Codec2Client, completely avoiding system resource query errors.
                initAudioTracks()
            } catch (e: Throwable) {
                Log.w("SoundManager", "AudioTrack background init warning", e)
            }
        }.start()
    }

    private fun initAudioTracks() {
        val tapPcm = generateTactileTapPcm()
        tapTrack = buildStaticAudioTrack(tapPcm, 44100, volume = 0.95f)

        val successPcm = generateAscendingChimePcm()
        successTrack = buildStaticAudioTrack(successPcm, 44100, volume = 0.95f)

        val blockedPcm = generateBlockedBumpPcm()
        blockedTrack = buildStaticAudioTrack(blockedPcm, 44100, volume = 0.85f)

        val undoPcm = generateUndoPcm()
        undoTrack = buildStaticAudioTrack(undoPcm, 44100, volume = 0.80f)

        val completePcm = generateCompleteFanfarePcm()
        completeTrack = buildStaticAudioTrack(completePcm, 44100, volume = 1.0f)

        val bgmPcm = generateAmbientBgmPcm()
        bgmTrack = buildStaticAudioTrack(
            pcmShorts = bgmPcm,
            sampleRate = 22050,
            usage = AudioAttributes.USAGE_GAME,
            contentType = AudioAttributes.CONTENT_TYPE_MUSIC,
            isLooping = true,
            volume = 0.24f
        )

        if (isSoundEnabled) {
            startBgm()
        }
    }

    private fun buildStaticAudioTrack(
        pcmShorts: ShortArray,
        sampleRate: Int,
        usage: Int = AudioAttributes.USAGE_GAME,
        contentType: Int = AudioAttributes.CONTENT_TYPE_SONIFICATION,
        isLooping: Boolean = false,
        volume: Float = 1.0f
    ): AudioTrack? {
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(usage)
                .setContentType(contentType)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val bufferSize = pcmShorts.size * 2

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmShorts, 0, pcmShorts.size)

            if (isLooping) {
                track.setLoopPoints(0, pcmShorts.size, -1)
            }
            track.setVolume(volume)
            track
        } catch (e: Throwable) {
            Log.w("SoundManager", "Error creating static AudioTrack", e)
            null
        }
    }

    private fun playStaticTrack(track: AudioTrack?) {
        if (!isSoundEnabled || track == null) return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause()
                track.flush()
            }
            track.setPlaybackHeadPosition(0)
            track.play()
        } catch (_: Throwable) {}
    }

    fun playTap() = playStaticTrack(tapTrack)
    fun playSuccess() = playStaticTrack(successTrack)
    fun playBlocked() = playStaticTrack(blockedTrack)
    fun playUndo() = playStaticTrack(undoTrack)
    fun playButtonClick() = playStaticTrack(tapTrack)

    fun playLevelComplete() {
        if (!isSoundEnabled) return
        try {
            // Duck BGM during the victory celebration
            bgmTrack?.setVolume(0.04f)
            playStaticTrack(completeTrack)
            Thread {
                try {
                    Thread.sleep(1700)
                    if (isSoundEnabled) {
                        bgmTrack?.setVolume(0.24f)
                    }
                } catch (_: Throwable) {}
            }.start()
        } catch (_: Throwable) {}
    }

    fun startBgm() {
        if (!isSoundEnabled) return
        try {
            bgmTrack?.let { track ->
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.setVolume(0.24f)
                    track.play()
                }
            }
        } catch (_: Throwable) {}
    }

    fun pauseBgm() {
        try {
            bgmTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                }
            }
        } catch (_: Throwable) {}
    }

    fun resumeBgm() {
        if (isSoundEnabled) {
            startBgm()
        }
    }

    fun release() {
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
            bgmTrack = null

            tapTrack?.release(); tapTrack = null
            successTrack?.release(); successTrack = null
            blockedTrack?.release(); blockedTrack = null
            undoTrack?.release(); undoTrack = null
            completeTrack?.release(); completeTrack = null
        } catch (_: Throwable) {}
    }

    // --- In-Memory PCM Synthesizers (Zero File I/O, Zero Codec Queries) ---

    private fun generateTactileTapPcm(): ShortArray {
        val totalSamples = 1764 // 0.04s @ 44100Hz
        val samples = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / 44100.0
            val freq = 380.0 + (920.0 - 380.0) * exp(-t * 95.0)
            val clickBurst = if (t < 0.003) sin(2.0 * PI * 1800.0 * t) * 0.4 else 0.0
            val decay = exp(-t * 80.0)
            val s = ((sin(2.0 * PI * freq * t) * 0.7 + clickBurst) * decay * 0.7 * 32767.0).toInt().coerceIn(-32768, 32767)
            samples[i] = s.toShort()
        }
        return samples
    }

    private fun generateAscendingChimePcm(): ShortArray {
        val totalSamples = 9702 // 0.22s @ 44100Hz
        val samples = ShortArray(totalSamples)
        val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteDur = 0.22 / notes.size
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / 44100.0
            val noteIdx = ((t / noteDur).toInt()).coerceIn(0, notes.size - 1)
            val noteFreq = notes[noteIdx]
            val noteT = t - (noteIdx * noteDur)
            val noteEnv = if (noteT < 0.005) noteT / 0.005 else exp(-noteT * 14.0)
            val tone = sin(2.0 * PI * noteFreq * t) + 0.3 * sin(4.0 * PI * noteFreq * t)
            val whoosh = sin(2.0 * PI * (300.0 + 900.0 * (t / 0.22)) * t) * (1.0 - t / 0.22) * 0.2
            val s = ((tone * noteEnv * 0.55 + whoosh) * 0.65 * 32767.0).toInt().coerceIn(-32768, 32767)
            samples[i] = s.toShort()
        }
        return samples
    }

    private fun generateBlockedBumpPcm(): ShortArray {
        val totalSamples = 7056 // 0.16s @ 44100Hz
        val samples = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / 44100.0
            val b1 = if (t < 0.07) sin(2.0 * PI * 160.0 * t) * (1.0 - t / 0.07) else 0.0
            val b2 = if (t >= 0.07) {
                val t2 = t - 0.07
                sin(2.0 * PI * 115.0 * t2) * (1.0 - t2 / 0.09)
            } else 0.0
            val s = ((b1 * 0.8 + b2 * 0.6) * 0.55 * 32767.0).toInt().coerceIn(-32768, 32767)
            samples[i] = s.toShort()
        }
        return samples
    }

    private fun generateUndoPcm(): ShortArray {
        val totalSamples = 2646 // 0.06s @ 44100Hz
        val samples = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / 44100.0
            val freq = 480.0 - 180.0 * (t / 0.06)
            val env = 1.0 - (t / 0.06)
            val s = (sin(2.0 * PI * freq * t) * env * 0.45 * 32767.0).toInt().coerceIn(-32768, 32767)
            samples[i] = s.toShort()
        }
        return samples
    }

    private fun generateCompleteFanfarePcm(): ShortArray {
        val totalSamples = 70560 // 1.6s @ 44100Hz
        val samples = ShortArray(totalSamples)
        val chord1 = listOf(523.25, 659.25, 783.99)
        val chord2 = listOf(587.33, 739.99, 880.00)
        val chord3 = listOf(783.99, 987.77, 1174.66)
        val chord4 = listOf(523.25, 783.99, 1046.50, 1318.51)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / 44100.0
            val (chord, startTime) = when {
                t < 0.32 -> Pair(chord1, 0.0)
                t < 0.64 -> Pair(chord2, 0.32)
                t < 0.96 -> Pair(chord3, 0.64)
                else -> Pair(chord4, 0.96)
            }
            val localT = t - startTime
            val env = if (localT < 0.015) localT / 0.015 else exp(-localT * (if (startTime >= 0.96) 2.2 else 4.0))
            var chordMix = 0.0
            for ((idx, freq) in chord.withIndex()) {
                val weight = 1.0 - (idx * 0.12)
                chordMix += (sin(2.0 * PI * freq * t) + 0.25 * sin(4.0 * PI * freq * t)) * weight
            }
            chordMix /= chord.size.toDouble()
            val s = (chordMix * env * 0.65 * 32767.0).toInt().coerceIn(-32768, 32767)
            samples[i] = s.toShort()
        }
        return samples
    }

    private fun generateAmbientBgmPcm(): ShortArray {
        val totalSamples = 220500 // 10.0s @ 22050Hz
        val samples = ShortArray(totalSamples)
        val bassFreqs = listOf(130.81, 110.00, 87.31, 98.00)
        val padFreqs = listOf(
            listOf(261.63, 329.63, 392.00, 493.88),
            listOf(220.00, 261.63, 329.63, 392.00),
            listOf(174.61, 220.00, 261.63, 329.63),
            listOf(196.00, 246.94, 293.66, 392.00)
        )
        val melodyNotes = listOf(
            523.25, 659.25, 783.99, 659.25,
            493.88, 523.25, 659.25, 523.25,
            440.00, 523.25, 659.25, 523.25,
            392.00, 493.88, 587.33, 493.88
        )
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / 22050.0
            val barIdx = ((t / 2.5).toInt()).coerceIn(0, 3)
            val bass = sin(2.0 * PI * bassFreqs[barIdx] * t) * 0.28
            var pad = 0.0
            val activePad = padFreqs[barIdx]
            for (pf in activePad) {
                pad += sin(2.0 * PI * pf * t)
            }
            pad = (pad / activePad.size) * 0.22
            val noteIdx = ((t / 0.625).toInt()).coerceIn(0, melodyNotes.size - 1)
            val noteT = t - (noteIdx * 0.625)
            val kalimbaFreq = melodyNotes[noteIdx]
            val kalimbaEnv = if (noteT < 0.01) noteT / 0.01 else exp(-noteT * 4.5)
            val kalimba = (sin(2.0 * PI * kalimbaFreq * t) + 0.3 * sin(4.0 * PI * kalimbaFreq * t)) * kalimbaEnv * 0.25
            val loopFade = when {
                t < 0.08 -> t / 0.08
                t > 9.92 -> (10.0 - t) / 0.08
                else -> 1.0
            }
            val mixed = (bass + pad + kalimba) * loopFade * 0.45
            val s = (mixed * 32767.0).toInt().coerceIn(-32768, 32767)
            samples[i] = s.toShort()
        }
        return samples
    }
}
