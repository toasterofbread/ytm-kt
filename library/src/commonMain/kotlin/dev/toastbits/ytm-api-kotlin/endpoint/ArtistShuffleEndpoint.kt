package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.YoutubeApi

abstract class ArtistShuffleEndpoint: YoutubeApi.Endpoint() {
    data class RadioData(val items: List<SongData>, var continuation: String?)

    abstract suspend fun getArtistShuffle(
        artist: Artist,
        continuation: String?
    ): Result<RadioData>
}
