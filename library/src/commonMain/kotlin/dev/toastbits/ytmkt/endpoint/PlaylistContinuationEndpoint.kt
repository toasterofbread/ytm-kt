package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.radio.RadioContinuation

abstract class PlaylistContinuationEndpoint: ApiEndpoint() {
    abstract suspend fun getPlaylistContinuation(
        initial: Boolean,
        token: String,
        skip_initial: Int = 0,
    ): Result<Pair<List<YtmMediaItem>, RadioContinuation?>>
}
