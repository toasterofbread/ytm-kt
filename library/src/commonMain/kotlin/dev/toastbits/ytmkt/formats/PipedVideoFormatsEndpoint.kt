package dev.toastbits.ytmkt.formats

import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.YoutubeVideoFormat
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.call.body
import kotlinx.serialization.Serializable

class PipedVideoFormatsEndpoint(override val api: YtmApi): VideoFormatsEndpoint() {
    override suspend fun getVideoFormats(
        id: String,
        include_non_default: Boolean,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> = runCatching {
        val response: HttpResponse =
            api.client.request("https://pipedapi.kavin.rocks/streams/$id") {
                expectSuccess = false
                headers.append("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0")
            }

        val streams: PipedStreamsResponse = response.body()

        if (response.status.value !in 200 .. 299) {
            if (streams.error?.contains("YoutubeMusicPremiumContentException") == true) {
                throw YoutubeMusicPremiumContentException(streams.message)
            }

            throw RuntimeException(streams.message)
        }

        return@runCatching streams.audioStreams?.let { audio_streams ->
            if (filter != null) audio_streams.filter(filter) else audio_streams
        } ?: emptyList()
    }
}

@Serializable
private data class PipedStreamsResponse(
    val error: String?,
    val message: String?,
    val audioStreams: List<YoutubeVideoFormat>?,
    val relatedStreams: List<RelatedStream>?
) {
    @Serializable
    data class RelatedStream(val url: String, val type: String)
}
