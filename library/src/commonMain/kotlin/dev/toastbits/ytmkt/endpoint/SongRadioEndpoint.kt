package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong

abstract class SongRadioEndpoint: ApiEndpoint() {
    data class RadioData(val items: List<YtmSong>, var continuation: String?, val filters: List<List<RadioBuilderModifier>>?)

    abstract suspend fun getSongRadio(
        song_id: String,
        continuation: String?,
        filters: List<RadioBuilderModifier> = emptyList()
    ): Result<RadioData>
}
