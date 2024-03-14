package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.YoutubeApi

abstract class ArtistRadioEndpoint: YoutubeApi.Endpoint() {
    data class RadioData(val items: List<Song>, var continuation: String?)

    abstract suspend fun getArtistRadio(
        artist_id: String,
        continuation: String?
    ): Result<RadioData>
}
