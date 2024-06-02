package com.example.eep523_hw3

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var cityInput: EditText
    private lateinit var fetchButton: Button
    private lateinit var address: TextView
    private lateinit var status: TextView
    private lateinit var temp: TextView
    private lateinit var tempMin: TextView
    private lateinit var tempMax: TextView
    private lateinit var sunrise: TextView
    private lateinit var sunset: TextView
    private lateinit var travelAdvice: TextView
    private lateinit var loader: ProgressBar
    private lateinit var mainContainer: RelativeLayout
    private lateinit var errorText: TextView
    private val apiKey: String = "1813c046a30fb98fccca28c4d3bc94af"
    private var city: String = "Seattle,US"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityInput = findViewById(R.id.inputCity)
        fetchButton = findViewById(R.id.submitButton)
        address = findViewById(R.id.address)
        status = findViewById(R.id.status)
        temp = findViewById(R.id.temp)
        tempMin = findViewById(R.id.temp_min)
        tempMax = findViewById(R.id.temp_max)
        sunrise = findViewById(R.id.sunrise)
        sunset = findViewById(R.id.sunset)
        travelAdvice = findViewById(R.id.travelAdvice)
        loader = findViewById(R.id.loader)
        mainContainer = findViewById(R.id.mainContainer)
        errorText = findViewById(R.id.errorText)

        fetchButton.setOnClickListener {
            city = cityInput.text.toString()
            WeatherTask().execute()
        }

        WeatherTask().execute()
    }

    inner class WeatherTask : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            loader.visibility = View.VISIBLE
            mainContainer.visibility = View.GONE
            errorText.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey").readText(Charsets.UTF_8)
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (result == null) {
                    throw Exception("No response from server")
                }

                val jsonResponse = JSONObject(result)
                val main = jsonResponse.getJSONObject("main")
                val sys = jsonResponse.getJSONObject("sys")
                val weather = jsonResponse.getJSONArray("weather").getJSONObject(0)

                val temp = main.getString("temp")
                val tempMin = main.getString("temp_min")
                val tempMax = main.getString("temp_max")
                val weatherDescription = weather.getString("description")

                val addressText = jsonResponse.getString("name") + ", " + sys.getString("country")
                findViewById<TextView>(R.id.address).text = addressText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = "$temp°C"
                findViewById<TextView>(R.id.temp_min).text = "Min Temp: $tempMin°C"
                findViewById<TextView>(R.id.temp_max).text = "Max Temp: $tempMax°C"
                val sunRiseTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sys.getLong("sunrise") * 1000))
                val sunSetTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sys.getLong("sunset") * 1000))
                findViewById<TextView>(R.id.sunrise).text = "☀: $sunRiseTime"
                findViewById<TextView>(R.id.sunset).text = "🌙: $sunSetTime"

                val tempValue = temp.toDouble()
                val advice = when {
                    weatherDescription.contains("rain", true) -> "Don't forget to take an umbrella."
                    weatherDescription.contains("clear", true) -> "It's sunny, consider wearing sunglasses."
                    tempValue < 15 -> "It's cold, wear a jacket."
                    else -> "It's warm, a t-shirt should be fine."
                }
                findViewById<TextView>(R.id.travelAdvice).text = advice

                loader.visibility = View.GONE
                mainContainer.visibility = View.VISIBLE
            } catch (e: Exception) {
                loader.visibility = View.GONE
                errorText.text = "Error: ${e.message}"
                errorText.visibility = View.VISIBLE
            }
        }
    }
}