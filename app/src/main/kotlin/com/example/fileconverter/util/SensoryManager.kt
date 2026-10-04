package com.example.fileconverter.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Bodlai Sensory Engine
 * Provides precision tactile haptics, spring micro-interactions, and procedural
 * zero-latency mechanical acoustic feedback (zero external audio files needed).
 */
object SensoryManager {
    private const val PREFS_NAME = "bodlai_sensory_prefs"
    private const val KEY_HAPTICS = "key_haptics_enabled"
    private const val KEY_SOUND = "key_sound_enabled"

    private val _hapticsEnabled = mutableStateOf(true)
    val hapticsEnabled: State<Boolean> = _hapticsEnabled

    private val _soundEnabled = mutableStateOf(false)
    val soundEnabled: State<Boolean> = _soundEnabled

    private var clickTrack: AudioTrack? = null
    private var successTrack: AudioTrack? = null
    private var isAudioInitialized = false

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _hapticsEnabled.value = prefs.getBoolean(KEY_HAPTICS, true)
        _soundEnabled.value = prefs.getBoolean(KEY_SOUND, false)

        if (!isAudioInitialized) {
            isAudioInitialized = true
            CoroutineScope(Dispatchers.Default).launch {
                initProceduralAudio()
            }
        }
    }

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        _hapticsEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAPTICS, enabled)
            .apply()

        if (enabled) {
            click(context)
        }
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        _soundEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SOUND, enabled)
            .apply()

        if (enabled) {
            playMechanicalClick()
        }
    }

    /**
     * Subtle micro-vibration for sliders, tabs, or drag reordering
     */
    fun tick(context: Context, view: View? = null) {
        if (!_hapticsEnabled.value) return
        try {
            var handled = false
            if (view != null) {
                handled = view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            if (!handled) {
                val vibrator = getVibrator(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(10, 60))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(10)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile button press feedback
     */
    fun click(context: Context, view: View? = null) {
        if (_soundEnabled.value) {
            playMechanicalClick()
        }
        if (!_hapticsEnabled.value) return
        try {
            var handled = false
            if (view != null) {
                val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    HapticFeedbackConstants.CONTEXT_CLICK
                } else {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
                handled = view.performHapticFeedback(constant)
            }
            if (!handled) {
                val vibrator = getVibrator(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(18, 140))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(18)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Firm confirmation feedback for item deletion or destructive actions
     */
    fun heavyClick(context: Context, view: View? = null) {
        if (_soundEnabled.value) {
            playMechanicalClick()
        }
        if (!_hapticsEnabled.value) return
        try {
            var handled = false
            if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                handled = view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
            if (!handled) {
                val vibrator = getVibrator(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, 220))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Celebratory tactile double-pulse when a conversion completes
     */
    fun success(context: Context) {
        if (_soundEnabled.value) {
            playSuccessChime()
        }
        if (!_hapticsEnabled.value) return
        try {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 60, 50)
                val amplitudes = intArrayOf(0, 160, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 35, 60, 50), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Rejection buzz for failed actions
     */
    fun error(context: Context) {
        if (!_hapticsEnabled.value) return
        try {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 45, 50, 45)
                val amplitudes = intArrayOf(0, 200, 0, 200)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 45, 50, 45), -1)
            }
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // Procedural In-Memory Acoustic Synthesizer (Zero External Files)
    // -------------------------------------------------------------

    fun playMechanicalClick() {
        if (!_soundEnabled.value) return
        try {
            clickTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.stop()
                    track.reloadStaticData()
                    track.play()
                }
            }
        } catch (_: Exception) {}
    }

    fun playSuccessChime() {
        if (!_soundEnabled.value) return
        try {
            successTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.stop()
                    track.reloadStaticData()
                    track.play()
                }
            }
        } catch (_: Exception) {}
    }

    private fun initProceduralAudio() {
        try {
            val sampleRate = 44100

            // 1. Synthesize Mechanical Snap (~12ms)
            val clickBytes = generateMechanicalClickPcm(sampleRate)
            clickTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(clickBytes.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build().apply {
                    write(clickBytes, 0, clickBytes.size)
                }

            // 2. Synthesize Luxury Success Chime (~140ms: D5 587Hz -> A5 880Hz)
            val chimeBytes = generateSuccessChimePcm(sampleRate)
            successTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(chimeBytes.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build().apply {
                    write(chimeBytes, 0, chimeBytes.size)
                }
        } catch (_: Exception) {}
    }

    private fun generateMechanicalClickPcm(sampleRate: Int): ByteArray {
        val durationMs = 12
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ByteArray(numSamples * 2)
        val freq = 1400.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t * 550.0)
            val transient = if (i < (sampleRate * 2) / 1000) (sin(2.0 * PI * 3200.0 * t) * 0.4) else 0.0
            val wave = (sin(2.0 * PI * freq * t) * 0.7 + transient) * envelope
            val sample = (wave * 26000).toInt().coerceIn(-32768, 32767).toShort()

            buffer[2 * i] = (sample.toInt() and 0xFF).toByte()
            buffer[2 * i + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun generateSuccessChimePcm(sampleRate: Int): ByteArray {
        val durationMs = 140
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-progress * 3.8)
            val freq = if (progress < 0.35) 587.33 else 880.0 // D5 -> A5
            val wave = sin(2.0 * PI * freq * t) * envelope
            val sample = (wave * 22000).toInt().coerceIn(-32768, 32767).toShort()

            buffer[2 * i] = (sample.toInt() and 0xFF).toByte()
            buffer[2 * i + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return buffer
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
