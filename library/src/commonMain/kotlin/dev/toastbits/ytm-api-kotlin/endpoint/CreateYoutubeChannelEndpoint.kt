package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.YoutubeApi
import io.ktor.http.Headers

abstract class CreateYoutubeChannelEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun createYoutubeChannel(
        headers: Headers,
        channel_creation_token: String,
        params: Map<String, String>
    ): Result<Artist>
}
