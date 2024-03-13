package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.YoutubeChannelCreationFormEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi

class YTMYoutubeChannelCreationFormEndpoint(override val api: YoutubeMusicApi): YoutubeChannelCreationFormEndpoint() {
    override suspend fun getForm(
        headers: Headers,
        channel_creation_token: String,
    ): Result<YoutubeAccountCreationForm.ChannelCreationForm> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("channel/get_channel_creation_form")
            .headers(headers)
            postWithBody(
                mapOf(
                    "source" to "MY_CHANNEL_CHANNEL_CREATION_SOURCE",
                    "channelCreationToken" to channel_creation_token
                ),
                YoutubeApi.PostBodyContext.UI_LANGUAGE
            )
        }

        val data: YoutubeAccountCreationForm = response.body
        return@runCatching data.channelCreation.channelCreationForm
    }
}
