package dev.toastbits.ytmapi.formats

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.external.YoutubeVideoFormat
import dev.toastbits.ytmapi.fromJson
import io.ktor.client.request.*
import io.ktor.client.statement.*

class PipedVideoFormatsEndpoint(override val api: YoutubeApi): VideoFormatsEndpoint() {
    override suspend fun getVideoFormats(
        id: String,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> = runCatching {
        val response: HttpResponse =
            api.client.request("https://pipedapi.kavin.rocks/streams/$id") {
                expectSuccess = false
                headers.append(HttpHeaders.UserAgent, "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0")
            }

        val streams: PipedStreamsResponse =
            try {
                response.body
            }
            catch (e: Throwable) {
                throw RuntimeException("getVideoFormats for $id using Piped API failed", e)
            }

        if (!response.status.is_successful) {
            if (streams.error?.contains("YoutubeMusicPremiumContentException") == true) {
                throw YoutubeMusicPremiumContentException(streams.message)
            }

            throw RuntimeException(streams.message)
        }

        return@runCatching streams.audioStreams?.let { streams ->
            if (filter != null) streams.filter(filter) else streams
        } ?: emptyList()
    }
}

private data class PipedStreamsResponse(
    val error: String?,
    val message: String?,
    val audioStreams: List<YoutubeVideoFormat>?,
    val relatedStreams: List<RelatedStream>?
) {
    data class RelatedStream(val url: String, val type: String)
}
