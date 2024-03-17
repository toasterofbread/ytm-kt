package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.endpoint.AccountPlaylistsEndpoint
import dev.toastbits.ytmkt.endpoint.CreateAccountPlaylistEndpoint
import dev.toastbits.ytmkt.endpoint.DeleteAccountPlaylistEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import dev.toastbits.ytmkt.impl.youtubei.formatYoutubePlaylistId
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put

open class YTMAccountPlaylistsEndpoint(override val auth: YoutubeiAuthenticationState): AccountPlaylistsEndpoint() {
    override val api: YoutubeiApi get() = auth.api

    override suspend fun getAccountPlaylists(): Result<List<YtmPlaylist>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
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

            val item: YtmMediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is YtmPlaylist) {
                return@mapNotNull null
            }

            for (menu_item in it.musicTwoRowItemRenderer.menu?.menuRenderer?.items?.asReversed() ?: emptyList()) {
                if (item.id == "VLLM" || menu_item.menuNavigationItemRenderer?.icon?.iconType == "DELETE") {
                    return@mapNotNull item.copy(
                        owner_id = auth.own_channel_id
                    )
                }
            }

            return@mapNotNull item
        }
    }
}

open class YTMCreateAccountPlaylistEndpoint(override val auth: YoutubeiAuthenticationState): CreateAccountPlaylistEndpoint() {
    override val api: YoutubeiApi get() = auth.api

    override suspend fun createAccountPlaylist(
        title: String,
        description: String
    ): Result<String> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("playlist/create")
            addApiHeadersWithAuthenticated()
            postWithBody(YoutubeiPostBody.UI_LANGUAGE.getPostBody(api)) {
                put("title", title)
                put("description", description)
            }
        }

        val data: PlaylistCreateResponse = response.body()
        return@runCatching data.playlistId
    }

    @Serializable
    private class PlaylistCreateResponse(val playlistId: String)
}

open class YTMDeleteAccountPlaylistEndpoint(override val auth: YoutubeiAuthenticationState): DeleteAccountPlaylistEndpoint() {
    override suspend fun deleteAccountPlaylist(
        playlist_id: String
    ): Result<Unit> = runCatching {
        api.client.request {
            endpointPath("playlist/delete")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("playlistId", formatYoutubePlaylistId(playlist_id))
            }
        }
    }
}
