package com.example.chatgptapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*
import com.example.chatgptapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(
            this
        )
    }

    private val allowPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }


    private val client = OkHttpClient()
    lateinit var txtResponse: TextView
    lateinit var idTVQuestion: TextView
    lateinit var etQuestion: TextInputEditText
    lateinit var generateButton: MaterialButton
    lateinit var extractbutton: MaterialButton
    lateinit var voiceButton: MaterialButton
    private lateinit var voiceAssistant: VoiceAssistant
    private val RECORD_AUDIO_PERMISSION_CODE = 101

    private fun requestRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        etQuestion = findViewById(R.id.etQuestion)
        idTVQuestion = findViewById(R.id.idTVQuestion)
        txtResponse = findViewById(R.id.txtResponse)
        generateButton = findViewById(R.id.generateButton)
        extractbutton = findViewById(R.id.extractTextButton)
        voiceButton = findViewById(R.id.myButton)

        binding.myButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    speechRecognizer.stopListening()
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_DOWN -> {
                    startListen()
                    return@setOnTouchListener true
                }
                else -> {
                    return@setOnTouchListener true
                }
            }
        }



        /*voiceButton.setOnClickListener {
            voiceAssistant.startListening()
        }

        voiceAssistant = VoiceAssistant(this) { response ->
            etQuestion.setText(response)
            getResponse(response)
        }*/


        requestRecordAudioPermission()



        extractbutton.setOnClickListener {
            val intent = Intent(this,ExtractTextActivity::class.java)
            startActivity(intent)
        }

        generateButton.setOnClickListener {
            // Start the ImageGenerator activity when the button is clicked
            val intent = Intent(this, ImageGenerator::class.java)
            startActivity(intent)
        }

        etQuestion.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val question = etQuestion.text.toString().trim()
                Toast.makeText(this, question, Toast.LENGTH_SHORT).show()
                if (question.isNotEmpty()) {
                    getResponse(question)
                }
                true
            } else {
                false
            }
        }
    }

    fun startListen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object:RecognitionListener{
            override fun onReadyForSpeech(p0: Bundle?) {

            }

            override fun onBeginningOfSpeech() {
                binding.etQuestion.setText("Listening")
            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(p0: Int) {

            }

            override fun onResults(bundle: Bundle?) {
                bundle?.let {
                    val result = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    binding.etQuestion.setText(result?.get(0))
                }
            }

            override fun onPartialResults(p0: Bundle?) {

            }

            override fun onEvent(p0: Int, p1: Bundle?) {

            }
        })
        speechRecognizer.startListening(intent)
    }


    private fun getResponse(question: String) {
        idTVQuestion.text = question
        etQuestion.setText("")

        val apiKey = "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"
        val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"

        val requestBody = """
            {
                "prompt": "$question",
                "max_tokens": 500,
                "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", "API failed", e)
                runOnUiThread {
                    txtResponse.text = "API request failed."
                }
            }



            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (body != null) {
                        Log.v("Data", body)
                        val jsonObject = JSONObject(body)
                        val jsonArray = jsonObject.optJSONArray("choices")
                        if (jsonArray != null && jsonArray.length() > 0) {
                            val textResult = jsonArray.getJSONObject(0).optString("text", "No text found in response.")
                            runOnUiThread {
                                txtResponse.text = textResult
                            }
                        } else {
                            runOnUiThread {
                                txtResponse.text = "No 'choices' found in response."
                            }
                        }
                    } else {
                        Log.v("Data", "Empty response body")
                        runOnUiThread {
                            txtResponse.text = "Empty response body."
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Error", "JSON parsing failed", e)
                    runOnUiThread {
                        txtResponse.text = "Error parsing JSON response."
                    }
                }
            }
        })
    }

    fun getPermissionOverO(context: Context, call: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                call.invoke()
            } else {
                allowPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }

    }



}
