package com.example.dice_app

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var statusLight: View
    private lateinit var statusText: TextView
    private lateinit var checkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusLight = findViewById(R.id.status_light)
        statusText = findViewById(R.id.status_text)
        checkButton = findViewById(R.id.check_button)

        checkButton.setOnClickListener {
            // Ezen a ponton küldjük majd az adatokat a szerverre
            checkConnectionStatus()
        }
    }

    private fun checkConnectionStatus() {
        // Állapotkezelés: zöldről pirosra váltás
        statusLight.setBackgroundColor(Color.RED)
        statusText.text = "Piros: Probléma észlelve"
    }
}