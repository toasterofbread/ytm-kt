package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.endpoint.LikedPlaylistsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMLikedPlaylistsEndpoint(override val auth: YoutubeMusicAuthInfo): LikedPlaylistsEndpoint() {
    override suspend fun getLikedPlaylists(): Result<List<Playlist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", "FEmusic_liked_playlists")
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
            // Skip 'New playlist' item
            if (it.musicTwoRowItemRenderer?.navigationEndpoint?.browseEndpoint == null) {
                return@mapNotNull null
            }

            var item: MediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is Playlist) {
                return@mapNotNull null
            }

            for (menu_item in it.musicTwoRowItemRenderer.menu?.menuRenderer?.items?.asReversed() ?: emptyList()) {
                if (menu_item.menuNavigationItemRenderer?.icon?.iconType == "DELETE") {
                    item = null
                    break
                }
            }

            return@mapNotNull item as? Playlist
        }
    }
}
