package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.MediaItemLayout

abstract class SearchEndpoint: ApiEndpoint() {
    abstract suspend fun search(query: String, params: String? = null, non_music: Boolean = false): Result<SearchResults>
}

enum class SearchType {
    SONG, VIDEO, PLAYLIST, ALBUM, ARTIST;

    fun getDefaultParams(): String =
        when (this) {
            SONG ->     "EgWKAQIIAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            PLAYLIST -> "EgWKAQIoAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            ARTIST ->   "EgWKAQIgAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            VIDEO ->    "EgWKAQIQAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
            ALBUM ->    "EgWKAQIYAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
        }
}

data class SearchFilter(val type: SearchType, val params: String)
data class SearchResults(val categories: List<Pair<MediaItemLayout, SearchFilter?>>, val suggested_correction: String?)
