package com.example.dice_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.wifi.WifiManager
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
    private lateinit var wifiManager: WifiManager
    private var currentMacAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusLight = findViewById(R.id.status_light)
        statusText = findViewById(R.id.status_text)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Regisztrálja az aktuális Wi-Fi MAC-címet indításkor
        registerCurrentWifiMac()

        // Wi-Fi kapcsolat változás figyelése
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
    }

    // Lekéri és regisztrálja az aktuális Wi-Fi MAC-címet
    private fun registerCurrentWifiMac() {
        val macAddress = getCurrentWifiMacAddress()
        macAddress?.let {
            registerDevice(it, getCurrentDate())
            currentMacAddress = it
        }
    }

    // Lekéri a csatlakoztatott Wi-Fi AP MAC-címét
    private fun getCurrentWifiMacAddress(): String? {
        val info = wifiManager.connectionInfo
        return if (info != null && info.bssid != null) info.bssid else null
    }

    // Idő és dátum formázása
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Regisztrálja a MAC-címet a szerveren
    private fun registerDevice(macAddress: String, date: String) {
        val data = DeviceData(mac_address = macAddress, date = date)
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.instance.registerDevice(data).execute()
            if (response.isSuccessful) {
                // Sikeres regisztráció, esetleg további logika
            }
        }
    }

    // BroadcastReceiver a Wi-Fi kapcsolat változásának figyelésére
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newMacAddress = getCurrentWifiMacAddress()
            if (newMacAddress != null && newMacAddress != currentMacAddress) {
                registerDevice(newMacAddress, getCurrentDate())
                currentMacAddress = newMacAddress // Frissítjük az aktuális MAC-címet
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiReceiver) // BroadcastReceiver leiratkozás, amikor az Activity megszűnik
    }
}