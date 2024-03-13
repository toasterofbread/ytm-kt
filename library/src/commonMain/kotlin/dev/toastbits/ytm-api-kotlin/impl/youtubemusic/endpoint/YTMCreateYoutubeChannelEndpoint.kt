package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.artist.ArtistRef
import com.toasterofbread.spmp.ui.layout.youtubemusiclogin.CreateChannelResponse
import dev.toastbits.ytmapi.endpoint.CreateYoutubeChannelEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi

class YTMCreateYoutubeChannelEndpoint(override val api: YoutubeMusicApi): CreateYoutubeChannelEndpoint() {
    override suspend fun createYoutubeChannel(
        headers: Map<String, String>,
        channel_creation_token: String,
        params: Map<String, String>
    ): Result<Artist> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("channel/create_channel")
            headers(headers)
            postWithBody(
                mutableMapOf(
                    "channelCreationToken" to channel_creation_token
                ).apply {
                    for (param in params) {
                        if (param.value.isNotBlank()) {
                            put(param.key, param.value)
                        }
                    }
                }
            )
        }

        val data: CreateChannelResponse = response.body

        val browse_id: String =
            data.navigationEndpoint.browseEndpoint.browseId
            ?: throw NullPointerException("browseId is null")

        return@runCatching ArtistRef(browse_id)
    }
}
