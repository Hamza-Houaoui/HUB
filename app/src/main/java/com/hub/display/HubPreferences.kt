package com.hub.display

import android.content.Context

class HubPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("hub_prefs", Context.MODE_PRIVATE)

    fun saveCity(name: String, lat: Double, lon: Double) {
        prefs.edit()
            .putString("city_name", name)
            .putFloat("latitude", lat.toFloat())
            .putFloat("longitude", lon.toFloat())
            .apply()
    }

    fun getCityName(): String = prefs.getString("city_name", "") ?: ""
    fun getLatitude(): Double = prefs.getFloat("latitude", 0f).toDouble()
    fun getLongitude(): Double = prefs.getFloat("longitude", 0f).toDouble()

    fun saveSunTimes(sunriseMinutes: Int, sunsetMinutes: Int) {
        prefs.edit()
            .putInt("sunrise_minutes", sunriseMinutes)
            .putInt("sunset_minutes", sunsetMinutes)
            .apply()
    }

    fun getSunriseMinutes(): Int = prefs.getInt("sunrise_minutes", 360)   // default 06:00
    fun getSunsetMinutes(): Int = prefs.getInt("sunset_minutes", 1080)    // default 18:00
}
