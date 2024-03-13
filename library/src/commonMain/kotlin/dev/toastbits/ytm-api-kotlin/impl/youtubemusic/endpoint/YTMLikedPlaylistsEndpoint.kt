package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.endpoint.LikedPlaylistsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.YoutubeiShelfContentsItem

class YTMLikedPlaylistsEndpoint(override val auth: YoutubeMusicAuthInfo): LikedPlaylistsEndpoint() {
    override suspend fun getLikedPlaylists(): Result<List<Playlist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(mapOf("browseId" to "FEmusic_liked_playlists"))
        }
            
        val data: YoutubeiBrowseResponse = response.body

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

            var item: MediaItemData? = it.toMediaItemData(hl)?.first
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
