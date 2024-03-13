package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.YoutubeApi

abstract class ArtistRadioEndpoint: YoutubeApi.Endpoint() {
    data class RadioData(val items: List<SongData>, var continuation: String?)

    abstract suspend fun getArtistRadio(
        artist: Artist,
        continuation: String?
    ): Result<RadioData>
}
