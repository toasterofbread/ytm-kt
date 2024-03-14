package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.uistrings.UiString

data class HomeFeedLoadResult(
    val layouts: List<MediaItemLayout>,
    val ctoken: String?,
    val filter_chips: List<HomeFeedFilterChip>?
)

data class HomeFeedFilterChip(
    val text: UiString,
    val params: String
)

abstract class HomeFeedEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getHomeFeed(
        min_rows: Int = -1,
        allow_cached: Boolean = true,
        params: String? = null,
        continuation: String? = null
    ): Result<HomeFeedLoadResult>
}
