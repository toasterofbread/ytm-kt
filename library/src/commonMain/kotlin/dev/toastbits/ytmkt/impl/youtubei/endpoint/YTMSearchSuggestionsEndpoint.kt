package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.SearchSuggestion
import dev.toastbits.ytmkt.endpoint.SearchSuggestionsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable

open class YTMSearchSuggestionsEndpoint(override val api: YoutubeiApi): SearchSuggestionsEndpoint() {
    override suspend fun getSearchSuggestions(
        query: String
    ): Result<List<SearchSuggestion>> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("music/get_search_suggestions")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("input", query)
            }
        }

        val parsed: YoutubeiSearchSuggestionsResponse = response.body()

        val suggestions = parsed.getSuggestions()
        if (suggestions == null) {
            throw NullPointerException("Suggestions is null ($parsed)")
        }

        return@runCatching suggestions
    }
}

@Serializable
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

    @Serializable
    data class Content(val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer?)
    @Serializable
    data class SearchSuggestionsSectionRenderer(val contents: List<Suggestion>)
    @Serializable
    data class Suggestion(val searchSuggestionRenderer: SearchSuggestionRenderer?, val historySuggestionRenderer: SearchSuggestionRenderer?)
    @Serializable
    data class SearchSuggestionRenderer(val navigationEndpoint: NavigationEndpoint)
    @Serializable
    data class NavigationEndpoint(val searchEndpoint: SearchEndpoint)
    @Serializable
    data class SearchEndpoint(val query: String)
}
