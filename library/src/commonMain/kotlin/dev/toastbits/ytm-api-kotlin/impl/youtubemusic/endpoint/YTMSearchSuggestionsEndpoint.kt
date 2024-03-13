package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.SearchSuggestion
import dev.toastbits.ytmapi.endpoint.SearchSuggestionsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi

class YTMSearchSuggestionsEndpoint(override val api: YoutubeMusicApi): SearchSuggestionsEndpoint() {
    override suspend fun getSearchSuggestions(
        query: String
    ): Result<List<SearchSuggestion>> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("music/get_search_suggestions")
            addAuthApiHeaders()
            postWithBody(mapOf("input" to query))
        }

        val parsed: YoutubeiSearchSuggestionsResponse = response.body

        val suggestions = parsed.getSuggestions()
        if (suggestions == null) {
            throw NullPointerException("Suggestions is null ($parsed)")
        }

        return@runCatching suggestions
    }
}

private data class YoutubeiSearchSuggestionsResponse(
    val contents: List<Content>?
) {
    fun getSuggestions(): List<SearchSuggestion>? =
        contents?.firstOrNull()
            ?.searchSuggestionsSectionRenderer
            ?.contents
            ?.mapNotNull { suggestion ->
                if (suggestion.searchSuggestionRenderer != null) {
                    return@mapNotNull SearchSuggestion(suggestion.searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query, false)
                }
                else if (suggestion.historySuggestionRenderer != null) {
                    return@mapNotNull SearchSuggestion(suggestion.historySuggestionRenderer.navigationEndpoint.searchEndpoint.query, true)
                }
                return@mapNotNull null
            }

    data class Content(val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer?)
    data class SearchSuggestionsSectionRenderer(val contents: List<Suggestion>)
    data class Suggestion(val searchSuggestionRenderer: SearchSuggestionRenderer?, val historySuggestionRenderer: SearchSuggestionRenderer?)
    data class SearchSuggestionRenderer(val navigationEndpoint: NavigationEndpoint)
    data class NavigationEndpoint(val searchEndpoint: SearchEndpoint)
    data class SearchEndpoint(val query: String)
}
