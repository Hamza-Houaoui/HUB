package com.hub.display

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SettingsActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchBtn: Button
    private lateinit var resultsList: ListView
    private lateinit var currentCityText: TextView

    private val prefs by lazy { HubPreferences(this) }
    private val cities = mutableListOf<CityResult>()
    private lateinit var adapter: ArrayAdapter<String>

    private data class CityResult(
        val displayName: String,
        val latitude: Double,
        val longitude: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        searchInput = findViewById(R.id.searchInput)
        searchBtn = findViewById(R.id.searchBtn)
        resultsList = findViewById(R.id.resultsList)
        currentCityText = findViewById(R.id.currentCity)

        adapter = ArrayAdapter(this, R.layout.item_city, mutableListOf<String>())
        resultsList.adapter = adapter

        // Show current selection
        val saved = prefs.getCityName()
        if (saved.isNotEmpty()) {
            currentCityText.text = "Current: $saved"
        }

        // Search button
        searchBtn.setOnClickListener { performSearch() }

        // Keyboard "Search" action
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        // Select a city from results
        resultsList.setOnItemClickListener { _, _, position, _ ->
            val city = cities[position]
            prefs.saveCity(city.displayName, city.latitude, city.longitude)
            finish()   // go back to main display
        }
    }

    private fun performSearch() {
        val query = searchInput.text.toString().trim()
        if (query.isNotEmpty()) searchCity(query)
    }

    private fun searchCity(query: String) {
        searchBtn.isEnabled = false
        searchBtn.text = "..."

        Thread {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = URL(
                    "https://geocoding-api.open-meteo.com/v1/search" +
                            "?name=$encoded&count=10&language=en"
                )
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000

                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()

                val json = JSONObject(response)
                cities.clear()

                if (json.has("results")) {
                    val results = json.getJSONArray("results")
                    for (i in 0 until results.length()) {
                        val r = results.getJSONObject(i)
                        val name = r.getString("name")
                        val admin1 = r.optString("admin1", "")
                        val country = r.optString("country", "")

                        val display = buildString {
                            append(name)
                            if (admin1.isNotEmpty() && admin1 != name) append(", $admin1")
                            if (country.isNotEmpty()) append(", $country")
                        }

                        cities.add(
                            CityResult(
                                displayName = display,
                                latitude = r.getDouble("latitude"),
                                longitude = r.getDouble("longitude")
                            )
                        )
                    }
                }

                runOnUiThread {
                    adapter.clear()
                    adapter.addAll(cities.map { it.displayName })
                    adapter.notifyDataSetChanged()

                    if (cities.isEmpty()) {
                        currentCityText.text = "No results for \"$query\""
                    }
                    searchBtn.isEnabled = true
                    searchBtn.text = getString(R.string.search)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    currentCityText.text = "Network error – try again"
                    searchBtn.isEnabled = true
                    searchBtn.text = getString(R.string.search)
                }
            }
        }.start()
    }
}
