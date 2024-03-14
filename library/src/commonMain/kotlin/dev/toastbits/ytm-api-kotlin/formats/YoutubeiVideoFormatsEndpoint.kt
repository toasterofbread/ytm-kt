package dev.toastbits.ytmapi.formats

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.external.YoutubeFormatsResponse
import dev.toastbits.ytmapi.model.external.YoutubeVideoFormat
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.request
import io.ktor.client.call.body
import kotlinx.serialization.json.put

class YoutubeiVideoFormatsEndpoint(override val api: YoutubeApi): VideoFormatsEndpoint() {
    override suspend fun getVideoFormats(
        id: String,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> = runCatching {
        val response: HttpResponse =
            api.client.request {
                endpointPath("player")
                addAuthApiHeaders()
                postWithBody(YoutubeApi.PostBodyContext.ANDROID_MUSIC) {
                    put("videoId", id)
                    put("playlistId", null)
                }
            }

        val formats: YoutubeFormatsResponse = response.body()
        if (formats.streamingData == null) {
            return PipedVideoFormatsEndpoint(api).getVideoFormats(id, filter)
        }

        val streaming_data: YoutubeFormatsResponse.StreamingData = formats.streamingData
        return@runCatching streaming_data.adaptiveFormats.mapNotNull { format ->
            if (filter?.invoke(format) == false) {
                return@mapNotNull null
            }

            format.copy(loudness_db = formats.playerConfig?.audioConfig?.loudnessDb)
        }
    }
}
