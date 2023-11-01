package com.example.chatgptapp

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.Toast
import java.util.*



class VoiceAssistant(val context: Context, val onResponse: (String) -> Unit) : OnInitListener {
    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val speechIntent = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
    val textToSpeech = TextToSpeech(context, this)


    fun startListening() {
        val intent = Intent(speechIntent)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speechRecognizer.startListening(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun handleRecognizedText(text: String) {
        onResponse(text)
    }


}
