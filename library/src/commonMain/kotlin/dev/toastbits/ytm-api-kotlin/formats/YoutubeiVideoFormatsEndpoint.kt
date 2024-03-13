package dev.toastbits.ytmapi.formats

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.YoutubeFormatsResponse
import dev.toastbits.ytmapi.model.external.YoutubeVideoFormat
import dev.toastbits.ytmapi.fromJson
import io.ktor.client.statement.HttpResponse

class YoutubeiVideoFormatsEndpoint(override val api: YoutubeApi): VideoFormatsEndpoint() {
    override suspend fun getVideoFormats(
        id: String,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> = runCatching {
        val response: HttpResponse =
            api.client.request {
                endpointPath("player")
                addAuthApiHeaders()
                postWithBody(
                    mapOf(
                        "videoId" to id,
                        "playlistId" to null
                    ),
                    YoutubeApi.PostBodyContext.ANDROID_MUSIC
                )
            }

        val formats: YoutubeFormatsResponse = repsonse.body
        if (formats.streamingData == null) {
            return PipedVideoFormatsEndpoint(api).getVideoFormats(id, filter)
        }

        val streaming_data: YoutubeFormatsResponse.StreamingData = formats.streamingData
        return streaming_data.adaptiveFormats.mapNotNull { format ->
            if (filter?.invoke(format) == false) {
                return@mapNotNull null
            }

            format.copy(loudness_db = formats.playerConfig?.audioConfig?.loudnessDb)
        }
    }
}
