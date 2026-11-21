package com.hub.display

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository {

    data class HourlyForecast(
        val time: String, // e.g. "02:00"
        val temp: Int,
        val weatherCode: Int,
        val isDay: Boolean,
        val precipitationProb: Int
    )

    data class WeatherData(
        val temperature: Double,
        val minTemp: Double,
        val maxTemp: Double,
        val feelsLike: Double,
        val description: String,
        val emoji: String,
        val weatherCode: Int,
        val isDay: Boolean,
        val humidity: Int,
        val windSpeed: Double,
        val uvIndex: Double,
        val precipitationProb: Int,
        val sunriseMinutes: Int,
        val sunsetMinutes: Int,
        val hourly: List<HourlyForecast>
    )

    /**
     * Fetches current weather + daily Min/Max + 24h hourly forecast.
     */
    fun getWeather(lat: Double, lon: Double): WeatherData? {
        val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,weather_code,wind_speed_10m,uv_index,is_day" +
                "&hourly=temperature_2m,weather_code,is_day,precipitation_probability" +
                "&forecast_hours=24" +
                "&daily=sunrise,sunset,temperature_2m_max,temperature_2m_min" +
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
            val feelsLike = current.optDouble("apparent_temperature", temp)
            val humidity = current.getInt("relative_humidity_2m")
            val weatherCode = current.getInt("weather_code")
            val isDay = current.optInt("is_day", 1) == 1
            val currentPrecip = current.optInt("precipitation_probability", 0)
            val windSpeed = current.optDouble("wind_speed_10m", 0.0)
            val uvIndex = current.optDouble("uv_index", 0.0)

            val daily = json.getJSONObject("daily")
            val sunrise = daily.getJSONArray("sunrise").getString(0)
            val sunset = daily.getJSONArray("sunset").getString(0)
            val maxT = daily.optJSONArray("temperature_2m_max")?.optDouble(0, temp) ?: temp
            val minT = daily.optJSONArray("temperature_2m_min")?.optDouble(0, temp) ?: temp

            val hourlyList = mutableListOf<HourlyForecast>()
            if (json.has("hourly")) {
                val hourlyJson = json.getJSONObject("hourly")
                val times = hourlyJson.getJSONArray("time")
                val temps = hourlyJson.getJSONArray("temperature_2m")
                val codes = hourlyJson.getJSONArray("weather_code")
                val isDays = hourlyJson.optJSONArray("is_day")
                val precips = hourlyJson.optJSONArray("precipitation_probability")

                val count = minOf(times.length(), 24)
                for (i in 0 until count) {
                    val fullTime = times.getString(i)
                    val hourStr = fullTime.substringAfter("T") // "14:00"
                    val t = temps.getDouble(i).toInt()
                    val c = codes.getInt(i)
                    val hDay = isDays?.optInt(i, 1) != 0
                    val pProb = precips?.optInt(i, 0) ?: 0
                    hourlyList.add(HourlyForecast(hourStr, t, c, hDay, pProb))
                }
            }

            WeatherData(
                temperature = temp,
                minTemp = minT,
                maxTemp = maxT,
                feelsLike = feelsLike,
                description = codeToDescription(weatherCode),
                emoji = codeToEmoji(weatherCode, isDay),
                weatherCode = weatherCode,
                isDay = isDay,
                humidity = humidity,
                windSpeed = windSpeed,
                uvIndex = uvIndex,
                precipitationProb = currentPrecip,
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
        fun codeToEmoji(code: Int, isDay: Boolean = true): String = when (code) {
            0 -> if (isDay) "☀️" else "🌙"
            1 -> if (isDay) "🌤️" else "🌑"
            2 -> if (isDay) "⛅" else "☁️"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌦️"
            56, 57 -> "🌧️"
            61, 63, 65 -> "🌧️"
            66, 67 -> "🌨️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌧️"
            85, 86 -> "🌨️"
            95, 96, 99 -> "⛈️"
            else -> if (isDay) "☀️" else "🌙"
        }

        fun codeToDescription(code: Int): String = when (code) {
            0 -> "Clear"
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
