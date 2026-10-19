package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val TAG = "JarvisSpeechManager"

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    var speechPitch: Float = 0.95f
    var speechRate: Float = 1.0f
    var isVoiceMuted: Boolean = false
    var currentLanguageCode: String = "en" // "en", "sw", "kam", "auto"

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            _rmsLevel.value = (rmsdB / 10f).coerceIn(0f, 1f)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            Log.w(TAG, "SpeechRecognizer error: $error")
                            _isListening.value = false
                            _rmsLevel.value = 0f
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _rmsLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognized = matches?.firstOrNull()
                            if (!recognized.isNullOrBlank()) {
                                onSpeechRecognized(recognized)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SpeechRecognizer", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            applyLanguageToTts(currentLanguageCode)
            isTtsReady = true
            tts?.setPitch(speechPitch)
            tts?.setSpeechRate(speechRate)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e(TAG, "TTS Initialization failed: $status")
        }
    }

    fun setLanguage(langCode: String) {
        currentLanguageCode = langCode
        if (isTtsReady) {
            applyLanguageToTts(langCode)
        }
    }

    private fun applyLanguageToTts(langCode: String) {
        val targetLocale = when (langCode.lowercase()) {
            "sw" -> Locale("sw", "KE")
            "kam" -> Locale("sw", "KE") // Fallback to Swahili phonetics if Kikamba TTS voice is absent
            else -> Locale.US
        }

        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Locale $targetLocale not supported, falling back to US")
            tts?.setLanguage(Locale.US)
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (isVoiceMuted || !isTtsReady || text.isBlank()) {
            _isSpeaking.value = false
            onDone?.invoke()
            return
        }

        // Auto-detect if text looks like Swahili
        if (text.contains("habari", ignoreCase = true) || text.contains("salama", ignoreCase = true) || text.contains("kazi", ignoreCase = true)) {
            applyLanguageToTts("sw")
        } else {
            applyLanguageToTts(currentLanguageCode)
        }

        tts?.setPitch(speechPitch)
        tts?.setSpeechRate(speechRate)
        val utteranceId = "JARVIS_${System.currentTimeMillis()}"

        _isSpeaking.value = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val langTag = when (currentLanguageCode.lowercase()) {
                "sw" -> "sw-KE"
                "kam" -> "sw-KE"
                else -> "en-US"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        }
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
