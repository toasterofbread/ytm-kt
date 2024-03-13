package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.endpoint.LikedArtistsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.YoutubeiShelfContentsItem

class YTMLikedArtistsEndpoint(override val auth: YoutubeMusicAuthInfo): LikedArtistsEndpoint() {
    override suspend fun getLikedArtists(): Result<List<ArtistData>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(mapOf("browseId" to "FEmusic_library_corpus_track_artists"))
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
                .musicShelfRenderer!!
                .contents!!

        return@runCatching playlist_data.mapNotNull {
            val item: MediaItemData? = it.toMediaItemData(hl)?.first
            if (item !is ArtistData) {
                return@mapNotNull null
            }
            return@mapNotNull item
        }
    }
}
