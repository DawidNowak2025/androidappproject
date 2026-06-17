package com.growgardentracker.android.data.remote.wiki

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WikiRepository(
    private val service: WikiApiService = WikiApiService()
) {
    suspend fun searchPlants(query: String): Result<List<WikiPage>> = withContext(Dispatchers.IO) {
        runCatching {
            if (query.isBlank()) emptyList() else service.searchPlants(query.trim()).pages
        }
    }
}
