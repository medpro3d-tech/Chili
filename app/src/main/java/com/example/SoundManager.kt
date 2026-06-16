package com.example

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin
import kotlin.math.floor

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool
    private var isLoaded = false

    private var soundRotar: Int = 0
    private var soundMover: Int = 0
    private var soundLinea: Int = 0
    private var soundGameOver: Int = 0
    private var soundCaida: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        // Generar tonos simples de sintetizador (para no requerir archivos externos)
        soundMover = loadGeneratedTone("mover.wav", 400.0, 50, type = "square")
        soundRotar = loadGeneratedTone("rotar.wav", 600.0, 80, type = "square")
        soundLinea = loadGeneratedTone("linea.wav", 880.0, 300, type = "sine")
        soundGameOver = loadGeneratedTone("gameover.wav", 200.0, 1000, type = "saw")
        soundCaida = loadGeneratedTone("caida.wav", 300.0, 80, type = "square")
    }

    private fun loadGeneratedTone(filename: String, frequency: Double, durationMs: Int, type: String): Int {
        val file = File(context.cacheDir, filename)
        if (!file.exists()) {
            val sampleRate = 44100
            val numSamples = (durationMs * sampleRate) / 1000
            val data = ByteArray(numSamples * 2)
            
            for (i in 0 until numSamples) {
                val time = i / sampleRate.toDouble()
                val value = when(type) {
                    "square" -> if (sin(2.0 * Math.PI * frequency * time) > 0) 1.0 else -1.0
                    "saw" -> 2.0 * (time * frequency - floor(time * frequency + 0.5))
                    else -> sin(2.0 * Math.PI * frequency * time) // "sine"
                }
                
                // Fade out (Evita clips al final)
                val envelope = 1.0 - (i.toDouble() / numSamples.toDouble())
                val pcm = (value * 32767 * envelope * 0.5).toInt()
                
                data[i * 2] = (pcm and 0xff).toByte()
                data[i * 2 + 1] = ((pcm shr 8) and 0xff).toByte()
            }
            
            val wavFile = generateWavHeader(sampleRate, 1, 16, data)
            FileOutputStream(file).use {
                it.write(wavFile)
            }
        }
        return soundPool.load(file.absolutePath, 1)
    }

    private fun generateWavHeader(sampleRate: Int, channels: Int, bitsPerSample: Int, data: ByteArray): ByteArray {
        val totalDataLen = data.size + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)
        
        // RIFF header
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        // format chunk size
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 
        header[20] = 1; header[21] = 0 // format (PCM)
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (data.size and 0xff).toByte()
        header[41] = ((data.size shr 8) and 0xff).toByte()
        header[42] = ((data.size shr 16) and 0xff).toByte()
        header[43] = ((data.size shr 24) and 0xff).toByte()
        
        return header + data
    }

    fun playRotar() = playSound(soundRotar)
    fun playMover() = playSound(soundMover)
    fun playLinea() = playSound(soundLinea)
    fun playGameOver() = playSound(soundGameOver)
    fun playCaida() = playSound(soundCaida)

    private fun playSound(soundId: Int) {
        if (isLoaded && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
