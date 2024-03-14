package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.LoadArtistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.DataParseException
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem.parseArtistResponse
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMLoadArtistEndpoint(override val api: YoutubeMusicApi): LoadArtistEndpoint() {
    override suspend fun loadArtist(artist_id: String): Result<Artist> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(YoutubeApi.PostBodyContext.MOBILE) {
                put("browseId", artist_id)
            }
        }

        return@runCatching parseArtistResponse(artist_id, response, hl, api).getOrThrow()
    }
}
