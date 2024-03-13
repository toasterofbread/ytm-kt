package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import com.toasterofbread.composekit.utils.common.indexOfOrNull
import dev.toastbits.ytmapi.model.external.mediaitem.ThumbnailProviderImpl
import dev.toastbits.ytmapi.model.external.mediaitem.enums.PlaylistType
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.endpoint.LoadPlaylistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.DataParseException
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.processDefaultResponse
import dev.toastbits.ytmapi.model.Header
import dev.toastbits.ytmapi.model.HeaderRenderer

private const val EMPTY_PLAYLIST_IMAGE_URL_PREFIX: String = "https://www.gstatic.com/youtube/media/ytm/images/pbg/playlist-empty-state"

private data class PlaylistUrlResponse(
    val microformat: Microformat?,
    val header: Header?
) {
    data class Microformat(val microformatDataRenderer: MicroformatDataRenderer)
    data class MicroformatDataRenderer(val urlCanonical: String?)
}

private fun formatBrowseId(browse_id: String): String =
    if (
        !browse_id.startsWith("VL")
        && !browse_id.startsWith("MPREb_")
    ) "VL$browse_id"
    else browse_id

class YTMLoadPlaylistEndpoint(override val api: YoutubeMusicApi): LoadPlaylistEndpoint() {
    override suspend fun loadPlaylist(
        playlist_id: String,
        continuation: MediaItemLayout.Continuation?,
        browse_params: String?,
        playlist_url: String?
    ): Result<Playlist> = runCatching {
        if (continuation != null) {
            continuation.loadContinuation(api.context).fold(
                {
                    val (items, ctoken) = it
                    playlist_data.items = playlist_data.items?.plus(items as List<SongData>)
                    return@runCatching playlist_data
                },
                { throw it }
            )
        }

        val title: String?
        var thumbnail_provider: ThumbnailProvider? = null

        var browse_id: String =
            if (browse_params == null) formatBrowseId(playlist_id)
            else playlist_id

        var loaded_playlist_url: String? = playlist_url

        if (loaded_playlist_url == null) {
            val response: HttpResponse = api.client.request {
                endpointPath("browse")
                addAuthApiHeaders()
                postWithBody(
                    mutableMapOf(
                        "browseId" to browse_id
                    ).apply {
                        browse_params?.also { params ->
                            put("params", params)
                        }
                    }
                )
            }

            val data: PlaylistUrlResponse = response.body

            loaded_playlist_url = data.microformat?.microformatDataRenderer?.urlCanonical

            val header_renderer: HeaderRenderer? = data.header?.getRenderer()
            if (header_renderer != null) {
                title = header_renderer.title?.firstTextOrNull()
                thumbnail_provider = ThumbnailProvider.fromThumbnails(header_renderer.getThumbnails())
            }
        }

        if (playlist_url != null) {
            val start: Int = playlist_url.indexOf("?list=") + 6
            val end: Int =
                playlist_url.indexOfOrNull("&", start) ?: playlist_url.length
            browse_id = formatBrowseId(playlist_url.substring(start, end))
        }

        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(
                mutableMapOf(
                    "browseId" to browse_id
                ).apply {
                    browse_params?.also { params ->
                        put("params", params)
                    }
                }
            )
        }

        processDefaultResponse(playlist_data, response, hl, api).onFailure {
            throw it
        }

        val provider: ThumbnailProvider? = playlist_data.thumbnail_provider
        if (
            thumbnail_provider is ThumbnailProviderImpl
            && (
                thumbnail_provider.url_a.startsWith(EMPTY_PLAYLIST_IMAGE_URL_PREFIX)
                || thumbnail_provider.url_b?.startsWith(EMPTY_PLAYLIST_IMAGE_URL_PREFIX) == true
            )
        ) {
            thumbnail_provider = null
        }

        return@runCatching playlist_data
    }
}
