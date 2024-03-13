package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.LoadArtistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.DataParseException
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.processDefaultResponse

class YTMLoadArtistEndpoint(override val api: YoutubeMusicApi): LoadArtistEndpoint() {
    override suspend fun loadArtist(artist_id: String): Result<ArtistData> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(
                mapOf(
                    "browseId" to artist_id
                ),
                YoutubeApi.PostBodyContext.MOBILE
            )
        }

        processDefaultResponse(artist_data, response, hl, api).onFailure {
            throw it
        }

        return@runCatching artist_data
    }
}
