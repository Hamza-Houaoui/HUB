package com.hub.display

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository {

    data class HourlyForecast(
        val time: String, // e.g. "02:00"
        val temp: Int,
        val emoji: String
    )

    data class WeatherData(
        val temperature: Double,
        val description: String,
        val emoji: String,
        val humidity: Int,
        val sunriseMinutes: Int,
        val sunsetMinutes: Int,
        val hourly: List<HourlyForecast>
    )

    /**
     * Fetches current weather and 24h hourly forecast from Open-Meteo.
     */
    fun getWeather(lat: Double, lon: Double): WeatherData? {
        val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,weather_code" +
                "&hourly=temperature_2m,weather_code" +
                "&forecast_hours=24" +
                "&daily=sunrise,sunset" +
                "&timezone=auto&forecast_days=2"

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

            val hourlyList = mutableListOf<HourlyForecast>()
            if (json.has("hourly")) {
                val hourlyJson = json.getJSONObject("hourly")
                val times = hourlyJson.getJSONArray("time")
                val temps = hourlyJson.getJSONArray("temperature_2m")
                val codes = hourlyJson.getJSONArray("weather_code")

                val count = minOf(times.length(), 24)
                for (i in 0 until count) {
                    val fullTime = times.getString(i)
                    val hourStr = fullTime.substringAfter("T") // "14:00"
                    val t = temps.getDouble(i).toInt()
                    val c = codes.getInt(i)
                    hourlyList.add(HourlyForecast(hourStr, t, codeToEmoji(c)))
                }
            }

            WeatherData(
                temperature = temp,
                description = codeToDescription(weatherCode),
                emoji = codeToEmoji(weatherCode),
                humidity = humidity,
                sunriseMinutes = parseTimeToMinutes(sunrise),
                sunsetMinutes = parseTimeToMinutes(sunset),
                hourly = hourlyList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            conn.disconnect()
        }
    }

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
            95, 96, 99 -> "⛈️"
            else -> "🌡️"
        }

        fun codeToDescription(code: Int): String = when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }
}
