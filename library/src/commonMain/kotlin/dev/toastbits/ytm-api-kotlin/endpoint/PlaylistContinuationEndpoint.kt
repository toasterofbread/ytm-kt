package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.YoutubeApi

abstract class PlaylistContinuationEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getPlaylistContinuation(
        initial: Boolean,
        token: String,
        skip_initial: Int = 0,
    ): Result<Pair<List<MediaItemData>, String?>>
}
