package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.radio.RadioContinuation

abstract class ArtistShuffleEndpoint: ApiEndpoint() {
    data class RadioData(val items: List<YtmSong>, var continuation: RadioContinuation?)

    abstract suspend fun getArtistShuffle(
        artist_shuffle_playlist_id: String,
        continuation: String?
    ): Result<RadioData>
}
