package com.example.dice_app

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusLight: TextView
    private lateinit var statusText: TextView
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusLight = findViewById(R.id.status_light)
        statusText = findViewById(R.id.status_text)
        actionButton = findViewById(R.id.action_button)

        val macAddress = getMacAddress()
        val currentDate = getCurrentDate()

        registerDevice(macAddress, currentDate)

        actionButton.setOnClickListener {
            checkStatus(macAddress, currentDate)
        }
    }

    private fun getMacAddress(): String {
        // Implementáld a MAC cím lekérését
        return "00:11:22:33:44:55" // Példa MAC cím
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun registerDevice(macAddress: String, date: String) {
        val data = DeviceData(mac_address = macAddress, date = date)

        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.instance.registerDevice(data).execute()
            if (response.isSuccessful) {
                // Sikeres regisztráció
            } else {
                // Hiba kezelése
            }
        }
    }

    private fun checkStatus(macAddress: String, date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.instance.checkStatus(macAddress, date).execute()
            if (response.isSuccessful) {
                val message = response.body()?.get("message")
                runOnUiThread {
                    if (message == "Status updated to red for matching devices") {
                        statusLight.setBackgroundColor(Color.RED)
                        statusText.text = "Piros: Probléma észlelve"
                    } else {
                        statusLight.setBackgroundColor(Color.GREEN)
                        statusText.text = "Zöld: Nincs probléma"
                    }
                }
            } else {
                // Hiba kezelése
            }
        }
    }
}