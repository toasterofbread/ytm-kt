package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.endpoint.LikedAlbumsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMLikedAlbumsEndpoint(override val auth: YoutubeMusicAuthInfo): LikedAlbumsEndpoint() {
    override suspend fun getLikedAlbums(): Result<List<Playlist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", "FEmusic_liked_albums")
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
                .gridRenderer!!
                .items

        return@runCatching playlist_data.mapNotNull {
            val item: MediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is Playlist || item.type != Playlist.Type.ALBUM) {
                return@mapNotNull null
            }
            return@mapNotNull item
        }
    }
}
