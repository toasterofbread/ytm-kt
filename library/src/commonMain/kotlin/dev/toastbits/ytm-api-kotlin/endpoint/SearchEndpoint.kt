package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.YoutubeApi

abstract class SearchEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun searchMusic(query: String, params: String?): Result<SearchResults>
}

enum class SearchType {
    SONG, VIDEO, PLAYLIST, ALBUM, ARTIST;

    fun getDefaultParams(): String =
        when (this) {
            SONG -> "EgWKAQIIAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            PLAYLIST -> "EgWKAQIoAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            ARTIST -> "EgWKAQIgAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            VIDEO -> "EgWKAQIQAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            ALBUM -> "EgWKAQIYAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
        }
}

data class SearchFilter(val type: SearchType, val params: String)
data class SearchResults(val categories: List<Pair<MediaItemLayout, SearchFilter?>>, val suggested_correction: String?)
