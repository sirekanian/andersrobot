package com.sirekanyan.andersrobot.api

import com.sirekanyan.andersrobot.config.Config
import com.sirekanyan.andersrobot.config.ConfigKey.WEATHER_API_KEY
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.telegram.telegrambots.meta.api.objects.Location

private const val BASE_URL = "https://api.openweathermap.org/data/2.5"
private const val WEATHER_URL = "$BASE_URL/weather"
private const val FORECAST_URL = "$BASE_URL/forecast"

class WeatherApi {

    private val httpClient = HttpClient()
    private val apiKey = Config[WEATHER_API_KEY]
    private val comparator = compareBy<Weather> { it.sys.country.lowercase().replace("ru", "aa") }.thenBy { it.name }

    fun getWeather(city: String, language: String?): Weather? = runBlocking {
        println("getting $city")
        fetchWeather("q" to city, "lang" to language)
    }

    fun getWeather(location: Location, language: String?): Weather? = runBlocking {
        println("getting $location")
        fetchWeather("lon" to location.longitude, "lat" to location.latitude, "lang" to language)
    }

    fun getWeathers(cities: List<Long>, language: String?): List<Weather> = runBlocking {
        println("getting $cities")
        cities.map { city -> async { fetchWeather("id" to city, "lang" to language) } }
            .mapNotNull { it.await() }
            .sortedWith(comparator)
    }

    fun getForecast(city: String, language: String?): Forecast? = runBlocking {
        println("getting $city forecast")
        fetchForecast("q" to city, "lang" to language)
    }

    private suspend fun fetchWeather(vararg params: Pair<String, Any?>): Weather? =
        fetch(WEATHER_URL, *params)

    private suspend fun fetchForecast(vararg params: Pair<String, Any?>): Forecast? =
        fetch(FORECAST_URL, *params)

    private suspend inline fun <reified T> fetch(url: String, vararg params: Pair<String, Any?>): T? =
        try {
            val response: String = httpClient.get(url) {
                params.forEach { (k, v) ->
                    if (v != null) {
                        parameter(k, v)
                    }
                }
                parameter("units", "metric")
                parameter("appid", apiKey)
            }.bodyAsText()
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString(response)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }

}