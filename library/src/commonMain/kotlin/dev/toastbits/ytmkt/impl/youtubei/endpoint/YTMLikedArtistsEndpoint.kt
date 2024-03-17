package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.endpoint.LikedArtistsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

open class YTMLikedArtistsEndpoint(override val auth: YoutubeiAuthenticationState): LikedArtistsEndpoint() {
    override val api: YoutubeiApi get() = auth.api

    override suspend fun getLikedArtists(): Result<List<YtmArtist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("browseId", "FEmusic_library_corpus_track_artists")
            }
        }

        val data: YoutubeiBrowseResponse = response.body()

        val playlist_data: List<YoutubeiShelfContentsItem> =
            data.contents!!
                .singleColumnBrowseResultsRenderer!!
                .tabs
                .first()
                .tabRenderer
                .content!!
                .sectionListRenderer!!
                .contents!!
                .first()
                .musicShelfRenderer!!
                .contents!!

        return@runCatching playlist_data.mapNotNull {
            val item: YtmMediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is YtmArtist) {
                return@mapNotNull null
            }

            if (item.id.startsWith("MPLA")) {
                return@mapNotNull item.copy(id = item.id.substring(4))
            }

            return@mapNotNull item
        }
    }
}
