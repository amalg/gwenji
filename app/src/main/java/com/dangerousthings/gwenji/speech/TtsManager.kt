package com.dangerousthings.gwenji.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
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
        private const val TAG = "GwenjiTTS"

        @Volatile
        private var INSTANCE: TtsManager? = null

        fun getInstance(context: Context): TtsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TtsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        Log.d(TAG, "Initializing TTS engine...")
        tts = TextToSpeech(context.applicationContext) { status ->
            Log.d(TAG, "TTS onInit callback, status=$status (SUCCESS=${TextToSpeech.SUCCESS})")
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                Log.d(TAG, "setLanguage result=$result (MISSING_DATA=${TextToSpeech.LANG_MISSING_DATA}, NOT_SUPPORTED=${TextToSpeech.LANG_NOT_SUPPORTED})")
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported or missing data")
                } else {
                    isInitialized = true
                    tts?.setSpeechRate(0.85f)
                    setupListener()
                    Log.d(TAG, "TTS initialized successfully")
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status=$status")
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
        Log.d(TAG, "speak() called: text='$text', isInitialized=$isInitialized, tts=$tts")
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized yet, cannot speak")
            return
        }
        if (text.isBlank()) {
            Log.w(TAG, "Text is blank, nothing to speak")
            return
        }
        onDone = onComplete
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gwenji_utterance")
        Log.d(TAG, "tts.speak() returned: $result (SUCCESS=${TextToSpeech.SUCCESS})")
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
