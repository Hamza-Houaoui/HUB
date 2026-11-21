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
    private lateinit var weatherTemp: TextView
    private lateinit var weatherDesc: TextView
    private lateinit var currentRainProb: TextView
    private lateinit var hourlyContainer: LinearLayout
    private lateinit var settingsBtn: ImageButton
    private lateinit var burnInOverlay: BurnInOverlay
    private lateinit var secondsDialView: SecondsDialView
    private lateinit var calendarBarView: CalendarBarView
    private lateinit var weatherAnimView: WeatherAnimationView
    private lateinit var animatedWeatherIcon: AnimatedWeatherIconView

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
        weatherTemp = findViewById(R.id.weatherTemp)
        weatherDesc = findViewById(R.id.weatherDesc)
        currentRainProb = findViewById(R.id.currentRainProb)
        hourlyContainer = findViewById(R.id.hourlyContainer)
        settingsBtn = findViewById(R.id.settingsBtn)
        burnInOverlay = findViewById(R.id.burnInOverlay)
        secondsDialView = findViewById(R.id.secondsDialView)
        calendarBarView = findViewById(R.id.calendarBarView)
        weatherAnimView = findViewById(R.id.weatherAnimView)
        animatedWeatherIcon = findViewById(R.id.animatedWeatherIcon)

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

        timeText.text = timeFmt.format(now.time)

        // Seconds rectangular frame update
        val sec = now.get(Calendar.SECOND)
        secondsDialView.setSecond(sec)

        // Center 8-Box Calendar update
        calendarBarView.updateDate(now, isNightMode)

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
        val dialInactive = if (night) Color.parseColor("#330A0A") else Color.parseColor("#222222")

        timeText.setTextColor(primary)
        weatherTemp.setTextColor(primary)
        weatherDesc.setTextColor(secondary)
        currentRainProb.setTextColor(if (night) Color.parseColor("#EF5350") else Color.parseColor("#4FC3F7"))
        settingsBtn.setColorFilter(tertiary)

        secondsDialView.setDialColors(primary, dialInactive)
        calendarBarView.updateDate(Calendar.getInstance(), night)
        renderHourly(lastHourlyData, night)
    }

    private fun renderHourly(hourlyList: List<WeatherRepository.HourlyForecast>, night: Boolean) {
        lastHourlyData = hourlyList
        hourlyContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val tempColor = if (night) Color.parseColor("#E53935") else Color.WHITE
        val timeColor = if (night) Color.parseColor("#B71C1C") else Color.parseColor("#AAAAAA")
        val rainColor = if (night) Color.parseColor("#EF5350") else Color.parseColor("#4FC3F7")

        for (item in hourlyList) {
            val view = inflater.inflate(R.layout.item_hourly_forecast, hourlyContainer, false)
            val tTime = view.findViewById<TextView>(R.id.hourlyTime)
            val tEmoji = view.findViewById<TextView>(R.id.hourlyEmoji)
            val tTemp = view.findViewById<TextView>(R.id.hourlyTemp)
            val tRain = view.findViewById<TextView>(R.id.hourlyRainProb)

            tTime.text = item.time
            tTime.setTextColor(timeColor)
            tEmoji.text = item.emoji
            tTemp.text = "${item.temp}°"
            tTemp.setTextColor(tempColor)

            if (item.precipitationProb > 0) {
                tRain.visibility = View.VISIBLE
                tRain.text = "💧${item.precipitationProb}%"
                tRain.setTextColor(rainColor)
            } else {
                tRain.visibility = View.GONE
            }

            hourlyContainer.addView(view)
        }
    }

    private fun fetchWeather() {
        val lat = prefs.getLatitude()
        val lon = prefs.getLongitude()

        if (lat == 0.0 && lon == 0.0) {
            weatherTemp.text = "--°"
            weatherDesc.text = "Tap ⚙ to set location"
            currentRainProb.visibility = View.GONE
            hourlyContainer.removeAllViews()
            return
        }

        Thread {
            try {
                val weather = WeatherRepository().getWeather(lat, lon)
                if (weather != null) {
                    prefs.saveSunTimes(weather.sunriseMinutes, weather.sunsetMinutes)

                    handler.post {
                        weatherTemp.text = "${weather.temperature.toInt()}°"
                        weatherDesc.text = weather.description

                        if (weather.precipitationProb > 0) {
                            currentRainProb.visibility = View.VISIBLE
                            currentRainProb.text = "💧 ${weather.precipitationProb}%"
                        } else {
                            currentRainProb.visibility = View.GONE
                        }

                        animatedWeatherIcon.setWeather(weather.emoji, weather.weatherCode, weather.isDay, isNightMode)
                        weatherAnimView.setWeatherState(weather.weatherCode, weather.isDay, isNightMode)
                        renderHourly(weather.hourly, isNightMode)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
