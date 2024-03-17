package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.YoutubeChannelCreationFormEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.external.YoutubeAccountCreationForm
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import kotlinx.serialization.json.put

open class YTMYoutubeChannelCreationFormEndpoint(override val api: YoutubeiApi): YoutubeChannelCreationFormEndpoint() {
    override suspend fun getForm(
        headers: Headers,
        channel_creation_token: String,
    ): Result<YoutubeAccountCreationForm.ChannelCreationForm> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("channel/get_channel_creation_form")
            headers {
                appendAll(headers)
            }
            postWithBody {
                put("source", "MY_CHANNEL_CHANNEL_CREATION_SOURCE")
                put("channelCreationToken", channel_creation_token)
            }
        }

        val data: YoutubeAccountCreationForm = response.body()
        return@runCatching data.channelCreation.channelCreationForm
    }
}
