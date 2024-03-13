package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import com.toasterofbread.spmp.ui.layout.youtubemusiclogin.YTAccountMenuResponse
import dev.toastbits.ytmapi.endpoint.UserAuthStateEndpoint

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
            return Result.failure(IllegalArgumentException("Missing the following headers: $missing_headers"))
        }

        val response: HttpResponse = api.client.request {
            endpointPath("account/account_menu")
            addAuthApiHeaders()
            headers {
                for (key in YoutubeMusicAuthInfo.REQUIRED_HEADERS) {
                    for (value in headers.values(key)) {
                        set(key, value)
                    }
                }
            }
            postWithBody()
        }
            
        val data: YTAccountMenuResponse = response.body

        val artist: Artist? = data.getAritst()
        if (artist == null) {
            throw YoutubeChannelNotCreatedException(headers, data.getChannelCreationToken())
        }

        return@runCatching YoutubeMusicAuthInfo.create(api, artist, headers)
    }
}
