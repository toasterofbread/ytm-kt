package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.endpoint.LikedAlbumsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

open class YTMLikedAlbumsEndpoint(override val auth: YoutubeiAuthenticationState): LikedAlbumsEndpoint() {
    override val api: YoutubeiApi get() = auth.api

    override suspend fun getLikedAlbums(): Result<List<YtmPlaylist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("browseId", "FEmusic_liked_albums")
            }
        }

        val data: YoutubeiBrowseResponse = response.body()

        val playlist_data: List<YoutubeiShelfContentsItem>? =
            (((((
                data.contents ?: throw RuntimeException("contents is null $data"))
                .singleColumnBrowseResultsRenderer ?: throw RuntimeException("singleColumnBrowseResultsRenderer is null $data"))
                .tabs
                .first()
                .tabRenderer
                .content ?: throw RuntimeException("tabRenderer.content is null $data"))
                .sectionListRenderer ?: throw RuntimeException("sectionListRenderer is null $data"))
                .contents ?: throw RuntimeException("sectionListRenderer.contents is null $data"))
                .first()
                .gridRenderer
                ?.items

        if (playlist_data == null) {
            return@runCatching emptyList()
        }

        return@runCatching playlist_data.mapNotNull {
            val item: YtmMediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is YtmPlaylist || item.type != YtmPlaylist.Type.ALBUM) {
                return@mapNotNull null
            }
            return@mapNotNull item
        }
    }
}
