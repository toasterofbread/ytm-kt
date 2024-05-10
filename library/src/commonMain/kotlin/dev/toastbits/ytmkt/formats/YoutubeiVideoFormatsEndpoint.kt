package dev.toastbits.ytmkt.formats

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiRequestData
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.YoutubeFormatsResponse
import dev.toastbits.ytmkt.model.external.YoutubeVideoFormat
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.request
import io.ktor.client.call.body
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.put

class YoutubeiVideoFormatsEndpoint(override val api: YtmApi): VideoFormatsEndpoint() {
    override suspend fun getVideoFormats(
        id: String,
        include_non_default: Boolean,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> {
        val main_result: Result<List<YoutubeVideoFormat>> = getVideoFormats(id, include_non_default, false, filter)
        if (main_result.isSuccess) {
            return main_result
        }

        val alt_result: Result<List<YoutubeVideoFormat>> = getVideoFormats(id, include_non_default, true, filter)
        if (alt_result.isSuccess) {
            return alt_result
        }

        return main_result
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getVideoFormats(
        id: String,
        include_non_default: Boolean,
        alt_method: Boolean,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> = runCatching {
        val response: HttpResponse =
            api.client.request {
                endpointPath("player", non_music_api = alt_method)
                addApiHeadersWithAuthenticated(non_music_api = alt_method)
                postWithBody(
                    if (alt_method) YoutubeiRequestData.getYtmContextWeb(YoutubeiRequestData.default_hl)
                    else YoutubeiRequestData.getYtmContextAndroidMusic(YoutubeiRequestData.default_hl)
                ) {
                    put("videoId", id)
                    put("playlistId", null)
                }
            }

        val formats: YoutubeFormatsResponse = response.body()
        if (formats.streamingData == null) {
            return PipedVideoFormatsEndpoint(api).getVideoFormats(id, include_non_default, filter)
        }

        val streaming_data: YoutubeFormatsResponse.StreamingData = formats.streamingData
        return@runCatching streaming_data.adaptiveFormats.mapNotNull { format ->
            if (!include_non_default && format?.audioTrack?.audioIsDefault == false) {
                return@mapNotNull null
            }

            if (filter?.invoke(format) == false) {
                return@mapNotNull null
            }

            format.copy(loudness_db = formats.playerConfig?.audioConfig?.loudnessDb)
        }
    }
}
