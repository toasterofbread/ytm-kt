package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.endpoint.LikedArtistsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMLikedArtistsEndpoint(override val auth: YoutubeMusicAuthInfo): LikedArtistsEndpoint() {
    override suspend fun getLikedArtists(): Result<List<Artist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
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
            val item: MediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is Artist) {
                return@mapNotNull null
            }
            return@mapNotNull item
        }
    }
}
