package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint

data class SearchSuggestion(
    val text: String,
    val is_from_history: Boolean
)

abstract class SearchSuggestionsEndpoint: ApiEndpoint() {
    abstract suspend fun getSearchSuggestions(query: String): Result<List<SearchSuggestion>>
}
