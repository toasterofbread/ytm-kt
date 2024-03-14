package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.YoutubeApi

abstract class ArtistShuffleEndpoint: YoutubeApi.Endpoint() {
    data class RadioData(val items: List<Song>, var continuation: String?)

    abstract suspend fun getArtistShuffle(
        artist_shuffle_playlist_id: String,
        continuation: String?
    ): Result<RadioData>
}
