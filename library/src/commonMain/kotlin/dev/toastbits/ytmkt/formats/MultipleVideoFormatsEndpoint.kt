package dev.toastbits.ytmkt.formats

import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.YoutubeVideoFormat

class MultipleVideoFormatsEndpoint(
    override val api: YtmApi,
    private val endpoints: List<VideoFormatsEndpoint> = getAllImplementationEndpoints(api)
): VideoFormatsEndpoint() {
    private var lastWorkingEndpoint: Int = 0

    override suspend fun getVideoFormats(
        id: String,
        include_non_default: Boolean,
        filter: ((YoutubeVideoFormat) -> Boolean)?
    ): Result<List<YoutubeVideoFormat>> {
        if (endpoints.isEmpty()) {
            return Result.failure(IllegalStateException("Endpoints list is empty"))
        }

        for (offset in endpoints.indices) {
            val endpointIndex: Int = (lastWorkingEndpoint + offset) % endpoints.size
            val endpoint: VideoFormatsEndpoint = endpoints[endpointIndex]

            val formats: List<YoutubeVideoFormat> =
                try {
                    endpoint.getVideoFormats(id, include_non_default, filter).getOrThrow()
                }
                catch (e: Throwable) {
                    if (offset + 1 == endpoints.size) {
                        return Result.failure(RuntimeException(e))
                    }
                    continue
                }

            lastWorkingEndpoint = endpointIndex

            if (formats.isNotEmpty()) {
                return Result.success(formats)
            }
        }

        return Result.success(emptyList())
    }

    companion object {
        fun getAllImplementationEndpoints(api: YtmApi): List<VideoFormatsEndpoint> =
            listOf(
                YoutubeiVideoFormatsEndpoint(api),
                PipedVideoFormatsEndpoint(api)
            ) + getPlatformSpecificVideoFormatsEndpoints(api)
    }
}

expect fun getPlatformSpecificVideoFormatsEndpoints(api: YtmApi): List<VideoFormatsEndpoint>
