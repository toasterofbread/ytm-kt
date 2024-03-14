package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.internal.YoutubeAccountMenuResponse
import dev.toastbits.ytmapi.endpoint.UserAuthStateEndpoint
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.request
import io.ktor.client.request.headers
import io.ktor.http.Headers

class YTMUserAuthStateEndpoint(
    override val api: YoutubeMusicApi
): UserAuthStateEndpoint() {
    override suspend fun byHeaders(
        headers: Headers
    ): Result<YoutubeMusicAuthInfo> = runCatching {
        val names: Set<String> = headers.names()
        val missing_headers: MutableList<String> = mutableListOf()

        for (header in YoutubeMusicAuthInfo.REQUIRED_HEADERS) {
            if (names.none { it.equals(header, ignoreCase = true) }) {
                missing_headers.add(header)
            }
        }

        if (missing_headers.isNotEmpty()) {
            throw IllegalArgumentException("Missing the following headers: $missing_headers")
        }

        val response: HttpResponse = api.client.request {
            endpointPath("account/account_menu")
            addAuthApiHeaders()
            headers {
                for (key in YoutubeMusicAuthInfo.REQUIRED_HEADERS) {
                    for (value in headers.getAll(key) ?: emptyList()) {
                        set(key, value)
                    }
                }
            }
            postWithBody()
        }

        val data: YoutubeAccountMenuResponse = response.body()

        val artist: Artist? = data.getAritst()
        if (artist == null) {
            throw YoutubeChannelNotCreatedException(headers, data.getChannelCreationToken())
        }

        return@runCatching YoutubeMusicAuthInfo.create(api, artist.id, headers)
    }
}
