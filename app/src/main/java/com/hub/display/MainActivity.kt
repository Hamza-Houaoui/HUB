package com.hub.display

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var weatherEmoji: TextView
    private lateinit var weatherTemp: TextView
    private lateinit var weatherDesc: TextView
    private lateinit var weatherCity: TextView
    private lateinit var hourlyContainer: LinearLayout
    private lateinit var settingsBtn: ImageButton
    private lateinit var burnInOverlay: BurnInOverlay

    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { HubPreferences(this) }
    private var isNightMode = false
    private var lastHourlyData: List<WeatherRepository.HourlyForecast> = emptyList()

    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1_000L)
        }
    }

    private val burnInRunnable = object : Runnable {
        override fun run() {
            burnInOverlay.flipPattern()
            handler.postDelayed(this, 5_000L)
        }
    }

    private val weatherRunnable = object : Runnable {
        override fun run() {
            fetchWeather()
            handler.postDelayed(this, 10 * 60 * 1_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        setContentView(R.layout.activity_main)

        timeText = findViewById(R.id.timeText)
        dateText = findViewById(R.id.dateText)
        weatherEmoji = findViewById(R.id.weatherEmoji)
        weatherTemp = findViewById(R.id.weatherTemp)
        weatherDesc = findViewById(R.id.weatherDesc)
        weatherCity = findViewById(R.id.weatherCity)
        hourlyContainer = findViewById(R.id.hourlyContainer)
        settingsBtn = findViewById(R.id.settingsBtn)
        burnInOverlay = findViewById(R.id.burnInOverlay)

        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(clockRunnable)
        handler.post(burnInRunnable)
        handler.post(weatherRunnable)
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockRunnable)
        handler.removeCallbacks(burnInRunnable)
        handler.removeCallbacks(weatherRunnable)
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    private fun updateClock() {
        val now = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())

        timeText.text = timeFmt.format(now.time)
        dateText.text = dateFmt.format(now.time)

        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val sunrise = prefs.getSunriseMinutes()
        val sunset = prefs.getSunsetMinutes()

        val shouldBeNight = currentMinutes >= sunset || currentMinutes < sunrise
        if (shouldBeNight != isNightMode) {
            isNightMode = shouldBeNight
            applyNightMode(isNightMode)
        }
    }

    private fun applyNightMode(night: Boolean) {
        val primary = if (night) Color.parseColor("#E53935") else Color.WHITE
        val secondary = if (night) Color.parseColor("#B71C1C") else Color.parseColor("#CCCCCC")
        val tertiary = if (night) Color.parseColor("#7F0000") else Color.parseColor("#888888")

        timeText.setTextColor(primary)
        dateText.setTextColor(secondary)
        weatherEmoji.setTextColor(primary)
        weatherTemp.setTextColor(primary)
        weatherDesc.setTextColor(secondary)
        weatherCity.setTextColor(tertiary)
        settingsBtn.setColorFilter(tertiary)

        // update hourly colors
        renderHourly(lastHourlyData, night)
    }

    private fun renderHourly(hourlyList: List<WeatherRepository.HourlyForecast>, night: Boolean) {
        lastHourlyData = hourlyList
        hourlyContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val tempColor = if (night) Color.parseColor("#E53935") else Color.WHITE
        val timeColor = if (night) Color.parseColor("#B71C1C") else Color.parseColor("#AAAAAA")

        for (item in hourlyList) {
            val view = inflater.inflate(R.layout.item_hourly_forecast, hourlyContainer, false)
            val tTime = view.findViewById<TextView>(R.id.hourlyTime)
            val tEmoji = view.findViewById<TextView>(R.id.hourlyEmoji)
            val tTemp = view.findViewById<TextView>(R.id.hourlyTemp)

            tTime.text = item.time
            tTime.setTextColor(timeColor)
            tEmoji.text = item.emoji
            tTemp.text = "${item.temp}°"
            tTemp.setTextColor(tempColor)

            hourlyContainer.addView(view)
        }
    }

    private fun fetchWeather() {
        val lat = prefs.getLatitude()
        val lon = prefs.getLongitude()

        if (lat == 0.0 && lon == 0.0) {
            weatherEmoji.text = ""
            weatherTemp.text = "--°"
            weatherDesc.text = "Tap ⚙ to set location"
            weatherCity.text = ""
            hourlyContainer.removeAllViews()
            return
        }

        Thread {
            try {
                val weather = WeatherRepository().getWeather(lat, lon)
                if (weather != null) {
                    prefs.saveSunTimes(weather.sunriseMinutes, weather.sunsetMinutes)

                    handler.post {
                        weatherEmoji.text = weather.emoji
                        weatherTemp.text = "${weather.temperature.toInt()}°"
                        weatherDesc.text = weather.description
                        weatherCity.text = prefs.getCityName()
                        renderHourly(weather.hourly, isNightMode)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
