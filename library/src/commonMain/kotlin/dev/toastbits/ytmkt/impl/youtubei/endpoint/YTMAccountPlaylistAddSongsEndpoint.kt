package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.AccountPlaylistAddSongsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import dev.toastbits.ytmkt.impl.youtubei.formatYoutubePlaylistId
import io.ktor.client.request.request
import kotlinx.serialization.json.put
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.putJsonArray

open class YTMAccountPlaylistAddSongsEndpoint(override val auth: YoutubeiAuthenticationState): AccountPlaylistAddSongsEndpoint() {
    override suspend fun addSongs(
        playlist_id: String,
        song_ids: Collection<String>
    ): Result<Unit> = runCatching {
        if (song_ids.isEmpty()) {
            return@runCatching Unit
        }

        api.client.request {
            endpointPath("browse/edit_playlist")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("playlistId", formatYoutubePlaylistId(playlist_id))

                putJsonArray("actions") {
                    for (id in song_ids) {
                        addJsonObject {
                            put("action", "ACTION_ADD_VIDEO")
                            put("addedVideoId", id)
                            put("dedupeOption", "DEDUPE_OPTION_SKIP")
                        }
                    }
                }
            }
        }
    }
}
