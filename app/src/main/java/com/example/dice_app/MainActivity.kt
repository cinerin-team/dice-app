import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dice_app.DeviceData
import com.example.dice_app.R
import com.example.dice_app.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
            val macAddress = getMacAddress()
            val date = getCurrentDate()
            registerDevice(macAddress, date)
        }
    }

    private fun registerDevice(macAddress: String, date: String) {
        val data = DeviceData(mac_address = macAddress, date = date)

        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.instance.registerDevice(data).execute()
            if (response.isSuccessful) {
                // Ellenőrzés sikeres, lekérjük az állapotot
                checkStatus(macAddress, date)
            }
        }
    }

    private fun checkStatus(macAddress: String, date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.instance.checkStatus(macAddress, date).execute()
            if (response.isSuccessful) {
                val message = response.body()?.get("message")
                // UI frissítése a fő szálon
                runOnUiThread {
                    if (message?.value == "Status updated to red for matching devices") {
                        statusLight.setBackgroundColor(Color.RED)
                        statusText.text = "Piros: Probléma észlelve"
                    } else {
                        statusLight.setBackgroundColor(Color.GREEN)
                        statusText.text = "Zöld: Nincs probléma"
                    }
                }
            }
        }
    }

    private fun getMacAddress(): String {
        // A megfelelő függvény a MAC-cím lekérésére
        return "00:1A:2B:3C:4D:5E"  // Helyettesítsd a valós MAC-címmel
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}