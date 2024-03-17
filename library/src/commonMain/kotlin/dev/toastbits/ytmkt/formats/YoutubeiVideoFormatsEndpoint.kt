package dev.toastbits.ytmkt.formats

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiRequestData
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.YoutubeFormatsResponse
import dev.toastbits.ytmkt.model.external.YoutubeVideoFormat
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.request
import io.ktor.client.call.body
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.put

class YoutubeiVideoFormatsEndpoint(override val api: YtmApi): VideoFormatsEndpoint() {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getVideoFormats(
        id: String,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> = runCatching {
        val response: HttpResponse =
            api.client.request {
                endpointPath("player")
                addApiHeadersWithAuthenticated()
                postWithBody(YoutubeiRequestData.getYtmContextAndroidMusic(YoutubeiRequestData.default_hl)) {
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
