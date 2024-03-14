package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.AccountPlaylistAddSongsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.impl.youtubemusic.formatYoutubePlaylistId
import io.ktor.client.request.request
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.putJsonArray

class YTMAccountPlaylistAddSongsEndpoint(override val auth: YoutubeMusicAuthInfo): AccountPlaylistAddSongsEndpoint() {
    override suspend fun addSongs(
        playlist_id: String,
        song_ids: Collection<String>
    ): Result<Unit> = runCatching {
        if (song_ids.isEmpty()) {
            return@runCatching Unit
        }

        api.client.request {
            endpointPath("browse/edit_playlist")
            addAuthApiHeaders()
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
