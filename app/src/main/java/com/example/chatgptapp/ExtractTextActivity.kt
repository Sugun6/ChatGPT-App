package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts


class ExtractTextActivity : AppCompatActivity() {

    private lateinit var imageview1: ImageView
    private var uriString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extract_text)

        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        imageview1 = findViewById(R.id.imageView)

        button1.setOnClickListener {
            mGetContent.launch("image/*")
        }

        button2.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("uri", uriString)
            startActivity(intent)
        }
    }

    private val mGetContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageview1.setImageURI(uri)
        uriString = uri.toString()
    }


}
