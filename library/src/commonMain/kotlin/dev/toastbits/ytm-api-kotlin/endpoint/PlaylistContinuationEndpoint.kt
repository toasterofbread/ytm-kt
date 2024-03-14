package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.radio.RadioContinuation

abstract class PlaylistContinuationEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getPlaylistContinuation(
        initial: Boolean,
        token: String,
        skip_initial: Int = 0,
    ): Result<Pair<List<MediaItem>, RadioContinuation?>>
}
