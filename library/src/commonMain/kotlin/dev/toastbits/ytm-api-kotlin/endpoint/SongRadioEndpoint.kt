package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.RadioBuilderModifier
import dev.toastbits.ytmapi.YoutubeApi

abstract class SongRadioEndpoint: YoutubeApi.Endpoint() {
    data class RadioData(val items: List<SongData>, var continuation: String?, val filters: List<List<RadioBuilderModifier>>?)

    abstract suspend fun getSongRadio(
        video_id: String,
        continuation: String?,
        filters: List<RadioBuilderModifier> = emptyList()
    ): Result<RadioData>
}
