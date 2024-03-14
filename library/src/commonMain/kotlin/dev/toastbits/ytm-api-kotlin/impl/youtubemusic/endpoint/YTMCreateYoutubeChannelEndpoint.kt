package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.internal.YoutubeCreateChannelResponse
import dev.toastbits.ytmapi.endpoint.CreateYoutubeChannelEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import kotlinx.serialization.json.put

class YTMCreateYoutubeChannelEndpoint(override val api: YoutubeMusicApi): CreateYoutubeChannelEndpoint() {
    override suspend fun createYoutubeChannel(
        headers: Headers,
        channel_creation_token: String,
        params: Map<String, String>
    ): Result<Artist> = runCatching {
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

        return@runCatching Artist(browse_id)
    }
}
