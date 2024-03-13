package dev.toastbits.ytmapi.formats

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.external.YoutubeVideoFormat

enum class VideoFormatsEndpointType {
    YOUTUBEI,
    PIPED;

    fun instantiate(api: YoutubeApi): VideoFormatsEndpoint =
        when(this) {
            YOUTUBEI -> YoutubeiVideoFormatsEndpoint(api)
            PIPED -> PipedVideoFormatsEndpoint(api)
        }

    companion object {
        val DEFAULT: VideoFormatsEndpointType = YOUTUBEI
    }
}

abstract class VideoFormatsEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getVideoFormats(id: String, filter: ((YoutubeVideoFormat) -> Boolean)? = null): Result<List<YoutubeVideoFormat>>

    class YoutubeMusicPremiumContentException(message: String?): RuntimeException(message)
}
