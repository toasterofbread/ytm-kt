package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.ThumbnailProviderImpl
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.endpoint.LoadPlaylistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.DataParseException
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem.parsePlaylistResponse
import dev.toastbits.ytmapi.model.internal.Header
import dev.toastbits.ytmapi.model.internal.HeaderRenderer
import dev.toastbits.ytmapi.radio.RadioContinuation
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

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
        continuation: RadioContinuation?,
        browse_params: String?,
        playlist_url: String?
    ): Result<Playlist> = runCatching {
        if (continuation != null) {
            val (items, cont) = continuation.loadContinuation(api).getOrThrow()
            return@runCatching Playlist(
                playlist_id,
                items = items.filterIsInstance<Song>(),
                continuation = cont
            )
        }

        var browse_id: String =
            if (browse_params == null) formatBrowseId(playlist_id)
            else playlist_id

        var loaded_playlist_url: String? = playlist_url

        if (loaded_playlist_url == null) {
            val response: HttpResponse = api.client.request {
                endpointPath("browse")
                addAuthApiHeaders()
                postWithBody {
                    put("browseId", browse_id)
                    if (browse_params != null) {
                        put("params", browse_params)
                    }
                }
            }

            val data: PlaylistUrlResponse = response.body()

            loaded_playlist_url = data.microformat?.microformatDataRenderer?.urlCanonical
        }

        if (loaded_playlist_url != null) {
            val start: Int = loaded_playlist_url.indexOf("?list=") + 6
            var end: Int = loaded_playlist_url.indexOf("&", start)
            if (end == -1) {
                end = loaded_playlist_url.length
            }

            browse_id = formatBrowseId(loaded_playlist_url.substring(start, end))
        }

        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", browse_id)
                if (browse_params != null) {
                    put("params", browse_params)
                }
            }
        }

        var playlist: Playlist = parsePlaylistResponse(playlist_id, response, hl, api).getOrThrow()

        val thumbnail_provider: ThumbnailProvider? = playlist.thumbnail_provider
        if (
            thumbnail_provider is ThumbnailProviderImpl
            && (
                thumbnail_provider.url_a.startsWith(EMPTY_PLAYLIST_IMAGE_URL_PREFIX)
                || thumbnail_provider.url_b?.startsWith(EMPTY_PLAYLIST_IMAGE_URL_PREFIX) == true
            )
        ) {
            playlist = playlist.copy(thumbnail_provider = null)
        }

        return@runCatching playlist
    }
}
