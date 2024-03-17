package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmkt.uistrings.UiString

data class SongFeedLoadResult(
    val layouts: List<MediaItemLayout>,
    val ctoken: String?,
    val filter_chips: List<SongFeedFilterChip>?
)

data class SongFeedFilterChip(
    val text: UiString,
    val params: String
) {
    companion object
}

abstract class SongFeedEndpoint: ApiEndpoint() {
    abstract suspend fun getSongFeed(
        min_rows: Int = -1,
        params: String? = null,
        continuation: String? = null
    ): Result<SongFeedLoadResult>
}
