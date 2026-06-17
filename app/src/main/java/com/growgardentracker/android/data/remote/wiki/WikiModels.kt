package com.growgardentracker.android.data.remote.wiki

import com.google.gson.annotations.SerializedName

data class WikiSearchResponse(
    val pages: List<WikiPage> = emptyList()
)

data class WikiPage(
    val id: Long = 0,
    val key: String = "",
    val title: String = "",
    val excerpt: String? = null,
    val description: String? = null,
    val thumbnail: WikiThumbnail? = null,
    @SerializedName("content_urls") val contentUrls: WikiContentUrls? = null
)

data class WikiThumbnail(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

data class WikiContentUrls(
    val desktop: WikiPageUrl? = null,
    val mobile: WikiPageUrl? = null
)

data class WikiPageUrl(
    val page: String? = null
)
