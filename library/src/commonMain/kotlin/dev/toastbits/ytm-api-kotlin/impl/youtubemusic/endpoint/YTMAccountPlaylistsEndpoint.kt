package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.AccountPlaylistsEndpoint
import dev.toastbits.ytmapi.endpoint.CreateAccountPlaylistEndpoint
import dev.toastbits.ytmapi.endpoint.DeleteAccountPlaylistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.impl.youtubemusic.formatYoutubePlaylistId
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.internal.YoutubeiShelfContentsItem
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMAccountPlaylistsEndpoint(override val auth: YoutubeMusicAuthInfo): AccountPlaylistsEndpoint() {
    override suspend fun getAccountPlaylists(): Result<List<Playlist>> = runCatching {
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

            val item: MediaItem? = it.toMediaItemData(hl, api)?.first
            if (item !is Playlist) {
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

private class PlaylistCreateResponse(val playlistId: String)

class YTMCreateAccountPlaylistEndpoint(override val auth: YoutubeMusicAuthInfo): CreateAccountPlaylistEndpoint() {
    override suspend fun createAccountPlaylist(
        title: String,
        description: String
    ): Result<String> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("playlist/create")
            addAuthApiHeaders()
            postWithBody(YoutubeApi.PostBodyContext.UI_LANGUAGE) {
                put("title", title)
                put("description", description)
            }
        }

        val data: PlaylistCreateResponse = response.body()
        return@runCatching data.playlistId
    }
}

class YTMDeleteAccountPlaylistEndpoint(override val auth: YoutubeMusicAuthInfo): DeleteAccountPlaylistEndpoint() {
    override suspend fun deleteAccountPlaylist(
        playlist_id: String
    ): Result<Unit> = runCatching {
        api.client.request {
            endpointPath("playlist/delete")
            addAuthApiHeaders()
            postWithBody {
                put("playlistId", formatYoutubePlaylistId(playlist_id))
            }
        }
    }
}
