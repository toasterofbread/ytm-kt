package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.YoutubeApi

abstract class CreateYoutubeChannelEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun createYoutubeChannel(
        headers: Map<String, String>,
        channel_creation_token: String,
        params: Map<String, String>
    ): Result<Artist>
}
