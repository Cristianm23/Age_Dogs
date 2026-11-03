package com.example.agedogs.data.repository

import com.example.agedogs.BuildConfig
import com.example.agedogs.data.model.Veterinaria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VeterinariaRepository {

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val apiKey = BuildConfig.MAPS_API_KEY

    suspend fun buscarVeterinariasCercanas(lat: Double, lon: Double, radioMetros: Int = 10000): Result<List<Veterinaria>> {
        return withContext(Dispatchers.IO) {
            try {
                // Google Places API Nearby Search
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lon&radius=$radioMetros&type=veterinary&key=$apiKey"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Error HTTP ${response.code} al consultar Google Places API"))
                }

                val bodyStr = response.body?.string() ?: "{}"
                val jsonElement = json.parseToJsonElement(bodyStr)
                val results = jsonElement.jsonObject["results"]?.jsonArray ?: emptyList()

                val lista = mutableListOf<Veterinaria>()
                for (el in results) {
                    val obj = el.jsonObject
                    val placeId = obj["place_id"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                    val name = obj["name"]?.jsonPrimitive?.content ?: "Clínica Veterinaria"
                    val address = obj["vicinity"]?.jsonPrimitive?.content ?: obj["formatted_address"]?.jsonPrimitive?.content ?: "Dirección disponible en mapa"

                    val geometry = obj["geometry"]?.jsonObject
                    val location = geometry?.get("location")?.jsonObject
                    val latNode = location?.get("lat")?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue
                    val lonNode = location?.get("lng")?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue

                    val phone = "Disponible en mapa"
                    val openingHours = obj["opening_hours"]?.jsonObject
                    val openNow = openingHours?.get("open_now")?.jsonPrimitive?.content?.toBoolean() ?: true
                    val es24Horas = name.contains("24", true) || name.contains("24h", true)

                    val distancia = calcularDistanciaKm(lat, lon, latNode, lonNode)

                    lista.add(
                        Veterinaria(
                            id = placeId,
                            nombre = name,
                            direccion = address,
                            telefono = phone,
                            lat = latNode,
                            lon = lonNode,
                            es24Horas = es24Horas,
                            distanciaKm = distancia,
                            abierta = openNow
                        )
                    )
                }

                Result.success(lista.sortedBy { it.distanciaKm })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun buscarPorDireccionOGps(query: String): Result<Pair<Double, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encoded&key=$apiKey"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Error en Google Geocoding API"))
                }

                val bodyStr = response.body?.string() ?: "{}"
                val jsonElement = json.parseToJsonElement(bodyStr)
                val results = jsonElement.jsonObject["results"]?.jsonArray ?: emptyList()

                if (results.isEmpty()) {
                    return@withContext Result.failure(Exception("No se encontró la ubicación ingresada"))
                }

                val first = results[0].jsonObject
                val geometry = first["geometry"]?.jsonObject
                val location = geometry?.get("location")?.jsonObject
                val lat = location?.get("lat")?.jsonPrimitive?.content?.toDoubleOrNull() ?: throw Exception("Latitud inválida")
                val lon = location?.get("lng")?.jsonPrimitive?.content?.toDoubleOrNull() ?: throw Exception("Longitud inválida")

                Result.success(Pair(lat, lon))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
