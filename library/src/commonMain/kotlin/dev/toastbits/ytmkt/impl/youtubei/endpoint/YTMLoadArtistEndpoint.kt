package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.endpoint.LoadArtistEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.loadmediaitem.parseArtistResponse
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

open class YTMLoadArtistEndpoint(override val api: YoutubeiApi): LoadArtistEndpoint() {
    override suspend fun loadArtist(artist_id: String): Result<YtmArtist> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody(YoutubeiPostBody.MOBILE.getPostBody(api)) {
                put("browseId", artist_id)
            }
        }

        return@runCatching parseArtistResponse(artist_id, response, hl, api).getOrThrow()
    }
}
