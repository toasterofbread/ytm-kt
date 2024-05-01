package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.PlaylistEditor
import dev.toastbits.ytmkt.endpoint.AccountPlaylistEditorEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import dev.toastbits.ytmkt.impl.youtubei.formatYoutubePlaylistId
import io.ktor.client.request.request
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.JsonObject

open class YTMAccountPlaylistEditorEndpoint(override val auth: YoutubeiAuthenticationState): AccountPlaylistEditorEndpoint() {
    override fun getEditor(
        playlist_id: String,
        item_ids: List<String>,
        item_set_ids: List<String>
    ): PlaylistEditor {
        return AccountPlaylistEditor(playlist_id, item_ids, item_set_ids, auth, this)
    }
}

private class AccountPlaylistEditor(
    val playlist_id: String,
    item_ids: List<String>,
    item_set_ids: List<String>,
    val auth: YoutubeiAuthenticationState,
    val endpoint: AccountPlaylistEditorEndpoint
): PlaylistEditor {
    private val item_ids: MutableList<String> = item_ids.toMutableList()
    private val item_set_ids: MutableList<String> = item_set_ids.toMutableList()

    override fun canPerformDeletion(): Boolean = auth.DeleteAccountPlaylist.isImplemented()

    override suspend fun performDeletion(): Result<Unit> = runCatching {
        check(canPerformDeletion())
        auth.DeleteAccountPlaylist.deleteAccountPlaylist(playlist_id).getOrThrow()
    }

    override suspend fun performAndCommitActions(
        actions: List<PlaylistEditor.Action>
    ): Result<Unit> = runCatching {
        if (actions.isEmpty()) {
            return@runCatching Unit
        }

        with(endpoint) {
            api.client.request {
                endpointPath("browse/edit_playlist")
                addApiHeadersWithAuthenticated()
                postWithBody {
                    put("playlistId", formatYoutubePlaylistId(playlist_id))

                    putJsonArray("actions") {
                        for (action in actions) {
                            val request_data: JsonObject? = getActionRequestData(action)
                            if (request_data != null) {
                                add(request_data)
                            }
                        }
                    }
                }
            }

            for (action in actions) {
                when (action) {
                    is PlaylistEditor.Action.Add -> {
                        item_ids.add(action.song_id)
                        // TODO
                        // item_set_ids.add()
                    }
                    is PlaylistEditor.Action.Move -> {
                        item_ids.add(action.to, item_ids.removeAt(action.from))
                        item_set_ids.add(action.to, item_set_ids.removeAt(action.from))
                    }
                    is PlaylistEditor.Action.Remove -> {
                        item_ids.removeAt(action.index)
                        item_set_ids.removeAt(action.index)
                    }

                    is PlaylistEditor.Action.SetTitle -> {}
                    is PlaylistEditor.Action.SetDescription -> {}
                    is PlaylistEditor.Action.SetImage -> {}
                    is PlaylistEditor.Action.SetImageWidth -> {}
                }
            }
        }
    }

    private fun getActionRequestData(action: PlaylistEditor.Action): JsonObject? {
        when(action) {
            is PlaylistEditor.Action.SetTitle -> {
                return buildJsonObject {
                    put("action", "ACTION_SET_PLAYLIST_NAME")
                    put("playlistName", action.title)
                }
            }
            is PlaylistEditor.Action.SetDescription -> {
                return buildJsonObject {
                    put("action", "ACTION_SET_PLAYLIST_DESCRIPTION")
                    put("playlistDescription", action.description)
                }
            }
            is PlaylistEditor.Action.Add -> {
                return buildJsonObject {
                    put("action", "ACTION_ADD_VIDEO")
                    put("addedVideoId", action.song_id)
                    put("dedupeOption", "DEDUPE_OPTION_SKIP")
                }
            }
            is PlaylistEditor.Action.Move -> {
                check(action.from != action.to)

                return buildJsonObject {
                    put("action", "ACTION_MOVE_VIDEO_BEFORE")
                    put("setVideoId", item_set_ids[action.from])

                    val to_index: Int =
                        if (action.to > action.from) action.to + 1
                        else action.to

                    if (to_index in item_set_ids.indices) {
                        put("movedSetVideoIdSuccessor", item_set_ids[to_index])
                    }
                }
            }
            is PlaylistEditor.Action.Remove -> {
                return buildJsonObject {
                    put("action", "ACTION_REMOVE_VIDEO")
                    put("removedVideoId", item_ids[action.index])
                    put("setVideoId", item_set_ids[action.index])
                }
            }

            is PlaylistEditor.Action.SetImage -> return null
            is PlaylistEditor.Action.SetImageWidth -> return null
        }
    }
}
