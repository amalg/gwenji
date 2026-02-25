package com.dangerousthings.gwenji.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

data class SpeechProgress(
    val isSpeaking: Boolean = false,
    val currentCharStart: Int = -1,
    val currentCharEnd: Int = -1
)

class TtsManager private constructor(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val _progress = MutableStateFlow(SpeechProgress())
    val progress: StateFlow<SpeechProgress> = _progress
    private var onDone: (() -> Unit)? = null

    companion object {
        @Volatile
        private var INSTANCE: TtsManager? = null

        fun getInstance(context: Context): TtsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TtsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.US
                tts?.setSpeechRate(0.85f)
                setupListener()
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _progress.value = SpeechProgress(isSpeaking = true)
            }
            override fun onDone(utteranceId: String?) {
                _progress.value = SpeechProgress(isSpeaking = false)
                onDone?.invoke()
            }
            @Deprecated("Deprecated in API")
            override fun onError(utteranceId: String?) {
                _progress.value = SpeechProgress(isSpeaking = false)
            }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                _progress.value = SpeechProgress(isSpeaking = true, currentCharStart = start, currentCharEnd = end)
            }
        })
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) return
        onDone = onComplete
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gwenji_utterance")
    }

    fun stop() {
        tts?.stop()
        _progress.value = SpeechProgress()
    }

    fun getAvailableVoices(): List<Voice> =
        tts?.voices?.filter { it.locale.language == "en" }?.toList() ?: emptyList()

    fun setVoice(voice: Voice) { tts?.voice = voice }
    fun setSpeechRate(rate: Float) { tts?.setSpeechRate(rate) }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
