package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylist
import dev.toastbits.ytmapi.endpoint.AccountPlaylistAddSongsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.impl.youtubemusic.unit

class YTMAccountPlaylistAddSongsEndpoint(override val auth: YoutubeMusicAuthInfo): AccountPlaylistAddSongsEndpoint() {
    override suspend fun addSongs(
        playlist: RemotePlaylist, 
        song_ids: Collection<String>
    ): Result<Unit> = runCatching {
        if (song_ids.isEmpty()) {
            return@runCatching Unit
        }

        val actions: List<Map<String, String>> = 
            song_ids.map { id ->
                mapOf(
                    "action" to "ACTION_ADD_VIDEO",
                    "addedVideoId" to id,
                    "dedupeOption" to "DEDUPE_OPTION_SKIP"
                )
            }

        api.client.request {
            endpointPath("browse/edit_playlist")
            addAuthApiHeaders()
            postWithBody(
                mapOf(
                    "playlistId" to RemotePlaylist.formatYoutubeId(playlist.id),
                    "actions" to actions
                )
            )
        }
    }
}
