package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

class SoundManager(private val context: Context) {

    var isSoundEnabled: Boolean = true

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            generateAndLoadSounds()
        } catch (e: Throwable) {
            Log.e("SoundManager", "Failed to initialize SoundPool", e)
        }
    }

    private fun generateAndLoadSounds() {
        val cacheDir = context.cacheDir ?: return

        // 1. Tap sound (short crisp pop / click, 800Hz for 30ms)
        createWavFile(cacheDir, "sound_tap.wav", 800.0, 0.03, 0.3)?.let {
            soundMap["tap"] = soundPool?.load(it.absolutePath, 1) ?: 0
        }

        // 2. Successful arrow clearing sound (pleasant ascending chime chord, 523Hz, 659Hz, 784Hz for 120ms)
        createChordWavFile(cacheDir, "sound_success.wav", listOf(523.25, 659.25, 783.99), 0.12, 0.4)?.let {
            soundMap["success"] = soundPool?.load(it.absolutePath, 1) ?: 0
        }

        // 3. Invalid / Blocked move sound (low buzzy thud/buzz, 150Hz square for 180ms)
        createBuzzerWavFile(cacheDir, "sound_blocked.wav", 150.0, 0.18, 0.5)?.let {
            soundMap["blocked"] = soundPool?.load(it.absolutePath, 1) ?: 0
        }

        // 4. Undo sound (medium blip, 440Hz for 50ms)
        createWavFile(cacheDir, "sound_undo.wav", 440.0, 0.05, 0.3)?.let {
            soundMap["undo"] = soundPool?.load(it.absolutePath, 1) ?: 0
        }

        // 5. Level complete fanfare (triumphant ascending melody: C5, E5, G5, C6)
        createMelodyWavFile(cacheDir, "sound_complete.wav", listOf(523.25, 659.25, 783.99, 1046.50), 0.35, 0.5)?.let {
            soundMap["complete"] = soundPool?.load(it.absolutePath, 1) ?: 0
        }
    }

    private fun createWavFile(dir: File, filename: String, freq: Double, durationSec: Double, volume: Double): File? {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = if (t < 0.01) t / 0.01 else if (t > durationSec - 0.02) (durationSec - t) / 0.02 else 1.0
                val sample = (sin(2.0 * Math.PI * freq * t) * envelope * volume * 32767.0).toInt().toShort()
                pcmData[i * 2] = (sample.toInt() and 0xff).toByte()
                pcmData[i * 2 + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
            }

            val file = File(dir, filename)
            FileOutputStream(file).use { fos ->
                writeWavHeader(fos, sampleRate, pcmData.size)
                fos.write(pcmData)
            }
            return file
        } catch (_: Throwable) {
            return null
        }
    }

    private fun createChordWavFile(dir: File, filename: String, freqs: List<Double>, durationSec: Double, volume: Double): File? {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = if (t < 0.01) t / 0.01 else if (t > durationSec - 0.02) (durationSec - t) / 0.02 else 1.0
                var mixed = 0.0
                for (f in freqs) {
                    mixed += sin(2.0 * Math.PI * f * t)
                }
                mixed /= freqs.size.toDouble()
                val sample = (mixed * envelope * volume * 32767.0).toInt().toShort()
                pcmData[i * 2] = (sample.toInt() and 0xff).toByte()
                pcmData[i * 2 + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
            }

            val file = File(dir, filename)
            FileOutputStream(file).use { fos ->
                writeWavHeader(fos, sampleRate, pcmData.size)
                fos.write(pcmData)
            }
            return file
        } catch (_: Throwable) {
            return null
        }
    }

    private fun createBuzzerWavFile(dir: File, filename: String, freq: Double, durationSec: Double, volume: Double): File? {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = if (t < 0.01) t / 0.01 else if (t > durationSec - 0.02) (durationSec - t) / 0.02 else 1.0
                val raw = if (sin(2.0 * Math.PI * freq * t) >= 0) 1.0 else -1.0
                val sample = (raw * envelope * volume * 32767.0).toInt().toShort()
                pcmData[i * 2] = (sample.toInt() and 0xff).toByte()
                pcmData[i * 2 + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
            }

            val file = File(dir, filename)
            FileOutputStream(file).use { fos ->
                writeWavHeader(fos, sampleRate, pcmData.size)
                fos.write(pcmData)
            }
            return file
        } catch (_: Throwable) {
            return null
        }
    }

    private fun createMelodyWavFile(dir: File, filename: String, freqs: List<Double>, durationSec: Double, volume: Double): File? {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val noteDuration = durationSec / freqs.size

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val noteIndex = ((t / noteDuration).toInt()).coerceIn(0, freqs.size - 1)
                val freq = freqs[noteIndex]
                val localT = t - (noteIndex * noteDuration)
                val envelope = if (localT < 0.005) localT / 0.005 else if (localT > noteDuration - 0.01) (noteDuration - localT) / 0.01 else 1.0
                val sample = (sin(2.0 * Math.PI * freq * localT) * envelope * volume * 32767.0).toInt().toShort()
                pcmData[i * 2] = (sample.toInt() and 0xff).toByte()
                pcmData[i * 2 + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
            }

            val file = File(dir, filename)
            FileOutputStream(file).use { fos ->
                writeWavHeader(fos, sampleRate, pcmData.size)
                fos.write(pcmData)
            }
            return file
        } catch (_: Throwable) {
            return null
        }
    }

    private fun writeWavHeader(out: FileOutputStream, sampleRate: Int, dataSize: Int) {
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val totalDataLen = dataSize + 36

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * (bitsPerSample / 8)).toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }

    private fun playSound(key: String) {
        if (!isSoundEnabled) return
        val soundId = soundMap[key] ?: return
        try {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        } catch (_: Throwable) {}
    }

    fun playTap() = playSound("tap")
    fun playSuccess() = playSound("success")
    fun playBlocked() = playSound("blocked")
    fun playUndo() = playSound("undo")
    fun playLevelComplete() = playSound("complete")
    fun playButtonClick() = playSound("tap")

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Throwable) {}
    }
}
