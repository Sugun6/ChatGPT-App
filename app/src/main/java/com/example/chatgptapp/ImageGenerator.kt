package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType


class ImageGenerator : AppCompatActivity() {


    private lateinit var inputText: EditText
    private lateinit var generateBtn: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_generator)

        inputText = findViewById(R.id.input_text)
        generateBtn = findViewById(R.id.generate_btn)
        progressBar = findViewById(R.id.progress_bar)
        imageView = findViewById(R.id.image_view)

        generateBtn.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isEmpty()) {
                inputText.error = "Text can't be empty"
                return@setOnClickListener
            }
            callAPI(text)
        }
    }

    private fun callAPI(text: String) {
        // API CALL
        setInProgress(true)
        val jsonBody = JSONObject()
        try {
            jsonBody.put("prompt", text)
            jsonBody.put("size", "256x256")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val requestBody = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .header("Authorization", "Bearer YOUR_API_KEY")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to generate image", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body?.string())
                    val imageUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url")
                    loadImage(imageUrl)
                    setInProgress(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun setInProgress(inProgress: Boolean) {
        runOnUiThread {
            if (inProgress) {
                progressBar.visibility = View.VISIBLE
                generateBtn.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                generateBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun loadImage(url: String) {
        // Load image
        runOnUiThread {
            Picasso.get().load(url).into(imageView)
        }
    }
}

