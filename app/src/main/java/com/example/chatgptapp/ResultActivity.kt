package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.net.Uri
import android.widget.EditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class ResultActivity : AppCompatActivity() {

    private lateinit var edittext1: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        edittext1 = findViewById(R.id.edittext1)

        val uriString = intent.getStringExtra("uri")
        val uri = Uri.parse(uriString)
        extractTextFromUri(applicationContext, uri)
    }

    private fun extractTextFromUri(context: Context, uri: Uri) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val image = InputImage.fromFilePath(context, uri)
            val result: com.google.android.gms.tasks.Task<Text> = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully
                    edittext1.setText(visionText.text)
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}