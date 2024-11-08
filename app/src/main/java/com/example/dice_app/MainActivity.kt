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
import android.util.Log
import androidx.lifecycle.lifecycleScope

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private val LOCATION_PERMISSION_REQUEST_CODE = 1


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

        // Indításkor ellenőrizzük a MAC-cím színét
        checkDeviceColor()
    }

    // Lekéri és regisztrálja az aktuális Wi-Fi MAC-címet a megadott státusszal
    private fun registerCurrentWifiMac(status: String) {
        val macAddress = getCurrentWifiMacAddress()
        macAddress?.let {
            registerDevice(it, getCurrentDate(), status)
            currentMacAddress = it
        }
    }

    // Lekéri a csatlakoztatott Wi-Fi AP MAC-címét
    private fun getCurrentWifiMacAddress(): String? {
        val info = wifiManager.connectionInfo
        return if (info != null && info.bssid != null) info.bssid else null
    }

    private fun checkDeviceColor() {
        val macAddress = getCurrentWifiMacAddress()
        macAddress?.let {
            currentMacAddress = it
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.instance.getDeviceColor(it).execute()
                    if (response.isSuccessful && response.body() != null) {
                        val color = response.body()!!["color"]
                        runOnUiThread {
                            updateColor(color ?: "green")
                        }
                    } else {
                        // Ha nem található a MAC-cím, zöld színnel regisztráljuk
                        registerDevice("green")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Hálózati hiba: ${e.message}")
                }
            }
        }
    }

    private fun registerDevice(color: String) {
        val macAddress = currentMacAddress ?: return
        val data = DeviceData(mac_address = macAddress, date = getCurrentDate(), status = color)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.instance.registerDevice(data).execute()
                runOnUiThread {
                    updateColor(color)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Hálózati hiba a regisztrációnál: ${e.message}")
            }
        }
    }

    private fun updateColor(color: String) {
        when (color) {
            "green" -> statusLight.setBackgroundColor(Color.GREEN)
            "yellow" -> statusLight.setBackgroundColor(Color.YELLOW)
            "red" -> statusLight.setBackgroundColor(Color.RED)
            else -> statusLight.setBackgroundColor(Color.GRAY)
        }
        statusText.text = "Státusz: $color"
    }

    // Idő és dátum formázása
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Regisztrálja a MAC-címet a szerveren
    private fun registerDevice(macAddress: String, date: String, status: String) {
        val data = DeviceData(mac_address = macAddress, date = date, status = status)
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("RegisterDevice", "Coroutine elindult")
            try {
                val response = RetrofitClient.instance.registerDevice(data).execute()
                if (response.isSuccessful) {
                    Log.d("RegisterDevice", "Sikeres küldés: ${response.body()}")
                    runOnUiThread {
                        statusText.text = "Sikeres küldés"
                    }
                } else {
                    Log.d("RegisterDevice", "Szerver hiba: ${response.code()}")
                    runOnUiThread {
                        statusText.text = "Szerver hiba: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.d("RegisterDevice", "Hálózati hiba: ${e.message}")
                runOnUiThread {
                    statusText.text = "Hálózati hiba: ${e.message}"
                }
            }
        }
    }

    // BroadcastReceiver a Wi-Fi kapcsolat változásának figyelésére
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newMacAddress = getCurrentWifiMacAddress()
            if (newMacAddress != null && newMacAddress != currentMacAddress) {
                registerDevice(newMacAddress, getCurrentDate(), "yellow")
                currentMacAddress = newMacAddress // Frissítjük az aktuális MAC-címet
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiReceiver) // BroadcastReceiver leiratkozás, amikor az Activity megszűnik
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Kérjük az engedélyt a felhasználótól
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Ha az engedély már megvan, regisztráljuk a Wi-Fi MAC-címet
            registerCurrentWifiMac("yellow")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Engedély megadva
                registerCurrentWifiMac("yellow")
            } else {
                // Engedély megtagadva
                statusText.text = "Helymeghatározási engedély szükséges a MAC-címhez"
            }
        }
    }
}