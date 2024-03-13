package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylist
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongRef
import dev.toastbits.ytmapi.model.external.PlaylistEditor
import dev.toastbits.ytmapi.endpoint.AccountPlaylistEditorEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo

class YTMAccountPlaylistEditorEndpoint(override val auth: YoutubeMusicAuthInfo): AccountPlaylistEditorEndpoint() {
    override fun getEditor(playlist_id: String, item_set_ids: List<String>): PlaylistEditor {
        return AccountPlaylistEditor(playlist_id, playlist_id, auth, this)
    }
}

private class AccountPlaylistEditor(
    val playlist_id: String,
    val item_set_ids: List<String>,
    val auth: YoutubeMusicAuthInfo,
    val endpoint: AccountPlaylistEditorEndpoint
): PlaylistEditor(playlist, auth.api.context) {
    override fun canPerformDeletion(): Boolean = auth.DeleteAccountPlaylist.isImplemented()

    override suspend fun performDeletion(): Result<Unit> = runCatching {
        check(canPerformDeletion())
        return auth.DeleteAccountPlaylist.deleteAccountPlaylist(playlist.id).getOrThrow()
    }

    override suspend fun performAndCommitActions(
        actions: List<Action>
    ): Result<Unit> = runCatching {
        lazyAssert { playlist.isPlaylistEditable(context) }

        if (actions.isEmpty()) {
            return@runCatching Unit
        }

        val actions_request_data: List<Map<String, String>> = actions.map { getActionRequestData(it) }

        with(endpoint) {
            api.client.request {
                endpointPath("browse/edit_playlist")
                addAuthApiHeaders()
                postWithBody(
                    mapOf(
                        "playlistId" to RemotePlaylist.formatYoutubeId(playlist.id),
                        "actions" to actions_request_data
                    )
                )
            }
        }
    }

    private fun getActionRequestData(action: Action): Map<String, String> {
        when(action) {
            is Action.SetTitle -> {
                return mapOf(
                    "action" to "ACTION_SET_PLAYLIST_NAME",
                    "playlistName" to action.title
                )
            }
            is Action.Add -> {
                return mapOf(
                    "action" to "ACTION_ADD_VIDEO",
                    "addedVideoId" to action.song_id,
                    "dedupeOption" to "DEDUPE_OPTION_SKIP"
                )
            }
            is Action.Move -> {
                check(playlist is Playlist) { "$playlist is not a Playlist" }
                checkNotNull(playlist.item_set_ids) { "$playlist item set IDs have not been loaded" }

                val set_ids = playlist.item_set_ids!!.toMutableList()
                check(set_ids.size == playlist.items!!.size)
                check(action.from != action.to)

                val data = mutableMapOf(
                    "action" to "ACTION_MOVE_VIDEO_BEFORE",
                    "setVideoId" to set_ids[action.from]
                )

                val to_index = if (action.to > action.from) action.to + 1 else action.to
                if (to_index in set_ids.indices) {
                    data["movedSetVideoIdSuccessor"] = set_ids[to_index]
                }

                set_ids.add(action.to, set_ids.removeAt(action.from))
                playlist.item_set_ids = set_ids

                return data
            }
            is Action.Remove -> {
                check(playlist is Playlist) { "$playlist is not a Playlist" }
                checkNotNull(playlist.item_set_ids) { "$playlist item set IDs have not been loaded" }

                return mapOf(
                    "action" to "ACTION_REMOVE_VIDEO",
                    "removedVideoId" to playlist.items!![action.index].id,
                    "setVideoId" to playlist.item_set_ids!![action.index]
                )
            }
        }
    }
}
