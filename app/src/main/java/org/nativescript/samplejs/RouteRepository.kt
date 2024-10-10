package org.nativescript.samplejs

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

class RouteRepository(private val routeDao: RouteDao) {
    private val client = OkHttpClient()

    // ... (previous methods remain the same)

    suspend fun searchLocation(query: String): GeoPoint? {
        val url = "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=1"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val jsonData = response.body?.string()
        
        return jsonData?.let {
            val jsonArray = JSONObject(it).getJSONArray("results")
            if (jsonArray.length() > 0) {
                val result = jsonArray.getJSONObject(0)
                val lat = result.getDouble("lat")
                val lon = result.getDouble("lon")
                GeoPoint(lat, lon)
            } else {
                null
            }
        }
    }
}