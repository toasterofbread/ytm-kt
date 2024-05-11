package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.ThumbnailProviderImpl
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.endpoint.LoadPlaylistEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.loadmediaitem.parsePlaylistResponse
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.model.internal.Header
import dev.toastbits.ytmkt.radio.RadioContinuation
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable

private const val EMPTY_PLAYLIST_IMAGE_URL_PREFIX: String = "https://www.gstatic.com/youtube/media/ytm/images/pbg/playlist-empty-state"

@Serializable
private data class PlaylistUrlResponse(
    val microformat: Microformat?,
    val header: Header?
) {
    @Serializable
    data class Microformat(val microformatDataRenderer: MicroformatDataRenderer)
    @Serializable
    data class MicroformatDataRenderer(val urlCanonical: String?)
}

private fun formatBrowseId(browse_id: String): String =
    if (
        !browse_id.startsWith("VL")
        && !browse_id.startsWith("MPREb_")
    ) "VL$browse_id"
    else browse_id

open class YTMLoadPlaylistEndpoint(override val api: YoutubeiApi): LoadPlaylistEndpoint() {
    override suspend fun loadPlaylist(
        playlist_id: String,
        continuation: RadioContinuation?,
        browse_params: String?,
        playlist_url: String?,
        use_non_music_api: Boolean
    ): Result<YtmPlaylist> = runCatching {
        if (continuation != null) {
            val (items, cont) = continuation.loadContinuation(api).getOrThrow()
            return@runCatching YtmPlaylist(
                playlist_id,
                items = items.filterIsInstance<YtmSong>(),
                continuation = cont
            )
        }

        var browse_id: String =
            if (browse_params == null) formatBrowseId(playlist_id)
            else playlist_id

        var loaded_playlist_url: String? = playlist_url

        if (loaded_playlist_url == null) {
            val response: HttpResponse = api.client.request {
                endpointPath("browse", non_music_api = use_non_music_api)
                addApiHeadersWithAuthenticated(non_music_api = use_non_music_api)
                postWithBody(
                    (if (use_non_music_api) YoutubeiPostBody.WEB else YoutubeiPostBody.DEFAULT).getPostBody(api)
                ) {
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
            endpointPath("browse", non_music_api = use_non_music_api)
            addApiHeadersWithAuthenticated(non_music_api = use_non_music_api)
            postWithBody(
                (if (use_non_music_api) YoutubeiPostBody.WEB else YoutubeiPostBody.DEFAULT).getPostBody(api)
            ) {
                put("browseId", browse_id)
                if (browse_params != null) {
                    put("params", browse_params)
                }
            }
        }

        var playlist: YtmPlaylist =
            parsePlaylistResponse(playlist_id, response, hl, api).getOrThrow()
                .copy(playlist_url = loaded_playlist_url)

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
