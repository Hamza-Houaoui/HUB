package com.hub.display

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository {

    data class WeatherData(
        val temperature: Double,
        val description: String,
        val emoji: String,
        val humidity: Int,
        val sunriseMinutes: Int,
        val sunsetMinutes: Int
    )

    /**
     * Fetches current weather from Open-Meteo (free, no API key).
     * Must be called on a background thread.
     */
    fun getWeather(lat: Double, lon: Double): WeatherData? {
        val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,weather_code" +
                "&daily=sunrise,sunset" +
                "&timezone=auto&forecast_days=1"

        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        return try {
            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m")
            val humidity = current.getInt("relative_humidity_2m")
            val weatherCode = current.getInt("weather_code")

            val daily = json.getJSONObject("daily")
            val sunrise = daily.getJSONArray("sunrise").getString(0)
            val sunset = daily.getJSONArray("sunset").getString(0)

            WeatherData(
                temperature = temp,
                description = codeToDescription(weatherCode),
                emoji = codeToEmoji(weatherCode),
                humidity = humidity,
                sunriseMinutes = parseTimeToMinutes(sunrise),
                sunsetMinutes = parseTimeToMinutes(sunset)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            conn.disconnect()
        }
    }

    /** Parses "2024-01-15T06:30" → minutes since midnight */
    private fun parseTimeToMinutes(isoTime: String): Int {
        val timePart = isoTime.substringAfter("T")
        val parts = timePart.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    companion object {

        fun codeToEmoji(code: Int): String = when (code) {
            0 -> "☀️"
            1, 2, 3 -> "⛅"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌦️"
            56, 57 -> "🌧️"
            61, 63, 65 -> "🌧️"
            66, 67 -> "🌨️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌧️"
            85, 86 -> "🌨️"
            95 -> "⛈️"
            96, 99 -> "⛈️"
            else -> "🌡️"
        }

        fun codeToDescription(code: Int): String = when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45 -> "Foggy"
            48 -> "Rime fog"
            51 -> "Light drizzle"
            53 -> "Moderate drizzle"
            55 -> "Dense drizzle"
            56 -> "Freezing light drizzle"
            57 -> "Freezing dense drizzle"
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            66 -> "Freezing light rain"
            67 -> "Freezing heavy rain"
            71 -> "Slight snow"
            73 -> "Moderate snow"
            75 -> "Heavy snow"
            77 -> "Snow grains"
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Violent rain showers"
            85 -> "Slight snow showers"
            86 -> "Heavy snow showers"
            95 -> "Thunderstorm"
            96 -> "Thunderstorm with slight hail"
            99 -> "Thunderstorm with heavy hail"
            else -> "Unknown"
        }
    }
}
