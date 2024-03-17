package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong

abstract class ArtistRadioEndpoint: ApiEndpoint() {
    data class RadioData(val items: List<YtmSong>, var continuation: String?)

    abstract suspend fun getArtistRadio(
        artist_id: String,
        continuation: String?
    ): Result<RadioData>
}
