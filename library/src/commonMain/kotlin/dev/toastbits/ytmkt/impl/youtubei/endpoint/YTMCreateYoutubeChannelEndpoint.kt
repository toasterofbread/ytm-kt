package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.internal.YoutubeCreateChannelResponse
import dev.toastbits.ytmkt.endpoint.CreateYoutubeChannelEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import kotlinx.serialization.json.put

open class YTMCreateYoutubeChannelEndpoint(override val api: YoutubeiApi): CreateYoutubeChannelEndpoint() {
    override suspend fun createYoutubeChannel(
        headers: Headers,
        channel_creation_token: String,
        params: Map<String, String>
    ): Result<YtmArtist> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("channel/create_channel")
            headers {
                appendAll(headers)
            }
            postWithBody {
                put("channelCreationToken", channel_creation_token)
                for (param in params) {
                    if (param.value.isNotBlank()) {
                        put(param.key, param.value)
                    }
                }
            }
        }

        val data: YoutubeCreateChannelResponse = response.body()

        val browse_id: String =
            data.navigationEndpoint.browseEndpoint.browseId
            ?: throw NullPointerException("browseId is null")

        return@runCatching YtmArtist(browse_id)
    }
}
