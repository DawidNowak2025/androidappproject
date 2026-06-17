package com.growgardentracker.android.data.remote.wiki

import com.google.gson.Gson
import java.net.URLEncoder
import okhttp3.OkHttpClient
import okhttp3.Request

class WikiApiService(
    private val client: OkHttpClient = OkHttpClient(),
    private val gson: Gson = Gson()
) {
    fun searchPlants(query: String, limit: Int = 10): WikiSearchResponse {
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val url = "https://en.wikipedia.org/w/rest.php/v1/search/page?q=$encodedQuery&limit=$limit"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "GROWGardenTrackerAndroid/1.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Wikipedia search failed.")
            val body = response.body?.string().orEmpty()
            return gson.fromJson(body, WikiSearchResponse::class.java) ?: WikiSearchResponse()
        }
    }
}
