package com.hub.display

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
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
    private lateinit var settingsBtn: ImageButton
    private lateinit var burnInOverlay: BurnInOverlay

    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { HubPreferences(this) }
    private var isNightMode = false

    // ── Periodic runnables ──────────────────────────────────────

    /** Update clock every second */
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1_000L)
        }
    }

    /** Flip burn-in checkerboard every 5 seconds */
    private val burnInRunnable = object : Runnable {
        override fun run() {
            burnInOverlay.flipPattern()
            handler.postDelayed(this, 5_000L)
        }
    }

    /** Refresh weather every 10 minutes */
    private val weatherRunnable = object : Runnable {
        override fun run() {
            fetchWeather()
            handler.postDelayed(this, 10 * 60 * 1_000L)
        }
    }

    // ── Lifecycle ───────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on forever
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Fullscreen immersive
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        setContentView(R.layout.activity_main)

        timeText = findViewById(R.id.timeText)
        dateText = findViewById(R.id.dateText)
        weatherEmoji = findViewById(R.id.weatherEmoji)
        weatherTemp = findViewById(R.id.weatherTemp)
        weatherDesc = findViewById(R.id.weatherDesc)
        weatherCity = findViewById(R.id.weatherCity)
        settingsBtn = findViewById(R.id.settingsBtn)
        burnInOverlay = findViewById(R.id.burnInOverlay)

        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Restart all periodic tasks
        handler.post(clockRunnable)
        handler.post(burnInRunnable)
        handler.post(weatherRunnable)

        // Re-enter immersive mode
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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockRunnable)
        handler.removeCallbacks(burnInRunnable)
        handler.removeCallbacks(weatherRunnable)
    }

    // ── Clock ───────────────────────────────────────────────────

    private fun updateClock() {
        val now = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())

        timeText.text = timeFmt.format(now.time)
        dateText.text = dateFmt.format(now.time)

        // Night-mode check using stored sunrise/sunset
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val sunrise = prefs.getSunriseMinutes()
        val sunset = prefs.getSunsetMinutes()

        val shouldBeNight = currentMinutes >= sunset || currentMinutes < sunrise
        if (shouldBeNight != isNightMode) {
            isNightMode = shouldBeNight
            applyNightMode(isNightMode)
        }
    }

    // ── Night / Day mode ────────────────────────────────────────

    private fun applyNightMode(night: Boolean) {
        if (night) {
            val primary = Color.parseColor("#CC2200")
            val secondary = Color.parseColor("#882200")
            val tertiary = Color.parseColor("#661100")

            timeText.setTextColor(primary)
            dateText.setTextColor(secondary)
            weatherEmoji.setTextColor(primary)
            weatherTemp.setTextColor(primary)
            weatherDesc.setTextColor(secondary)
            weatherCity.setTextColor(tertiary)
            settingsBtn.setColorFilter(tertiary)
        } else {
            timeText.setTextColor(Color.WHITE)
            dateText.setTextColor(Color.parseColor("#BBBBBB"))
            weatherEmoji.setTextColor(Color.WHITE)
            weatherTemp.setTextColor(Color.WHITE)
            weatherDesc.setTextColor(Color.parseColor("#BBBBBB"))
            weatherCity.setTextColor(Color.parseColor("#888888"))
            settingsBtn.setColorFilter(Color.parseColor("#888888"))
        }
    }

    // ── Weather ─────────────────────────────────────────────────

    private fun fetchWeather() {
        val lat = prefs.getLatitude()
        val lon = prefs.getLongitude()

        if (lat == 0.0 && lon == 0.0) {
            weatherEmoji.text = ""
            weatherTemp.text = "--°"
            weatherDesc.text = "Tap ⚙ to set location"
            weatherCity.text = ""
            return
        }

        Thread {
            try {
                val weather = WeatherRepository().getWeather(lat, lon)
                if (weather != null) {
                    // Persist sunrise/sunset for night-mode
                    prefs.saveSunTimes(weather.sunriseMinutes, weather.sunsetMinutes)

                    handler.post {
                        weatherEmoji.text = weather.emoji
                        weatherTemp.text = "${weather.temperature.toInt()}°"
                        weatherDesc.text = weather.description
                        weatherCity.text = prefs.getCityName()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
