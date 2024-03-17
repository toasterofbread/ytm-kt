package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import io.ktor.http.Headers

abstract class CreateYoutubeChannelEndpoint: ApiEndpoint() {
    abstract suspend fun createYoutubeChannel(
        headers: Headers,
        channel_creation_token: String,
        params: Map<String, String>
    ): Result<YtmArtist>
}
