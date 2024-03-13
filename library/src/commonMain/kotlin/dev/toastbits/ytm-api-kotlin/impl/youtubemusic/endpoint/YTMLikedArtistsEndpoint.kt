package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.enums.PlaylistType
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.endpoint.LikedAlbumsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.YoutubeiShelfContentsItem

class YTMLikedAlbumsEndpoint(override val auth: YoutubeMusicAuthInfo): LikedAlbumsEndpoint() {
    override suspend fun getLikedAlbums(): Result<List<Playlist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(mapOf("browseId" to "FEmusic_liked_albums"))
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
            val item: MediaItemData? = it.toMediaItemData(hl)?.first
            if (item !is Playlist || item.playlist_type != PlaylistType.ALBUM) {
                return@mapNotNull null
            }
            return@mapNotNull item
        }
    }
}
