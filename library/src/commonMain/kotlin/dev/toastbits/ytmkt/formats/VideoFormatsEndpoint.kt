package dev.toastbits.ytmkt.formats

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.YoutubeVideoFormat

abstract class VideoFormatsEndpoint: ApiEndpoint() {
    abstract suspend fun getVideoFormats(id: String, include_non_default: Boolean = false, filter: ((YoutubeVideoFormat) -> Boolean)? = null): Result<List<YoutubeVideoFormat>>

    class YoutubeMusicPremiumContentException(message: String?): RuntimeException(message)
}
