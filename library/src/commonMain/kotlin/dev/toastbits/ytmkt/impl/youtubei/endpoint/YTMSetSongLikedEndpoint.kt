package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.SongLikedStatus
import dev.toastbits.ytmkt.endpoint.SetSongLikedEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import io.ktor.client.request.request
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.put

open class YTMSetSongLikedEndpoint(override val auth: YoutubeiAuthenticationState): SetSongLikedEndpoint() {
    override suspend fun setSongLiked(
        song_id: String,
        liked: SongLikedStatus
    ): Result<Unit> = runCatching {
        api.client.request {
            endpointPath(when (liked) {
                SongLikedStatus.NEUTRAL -> "like/removelike"
                SongLikedStatus.LIKED -> "like/like"
                SongLikedStatus.DISLIKED -> "like/dislike"
            })
            addApiHeadersWithAuthenticated()
            postWithBody {
                putJsonObject("target") {
                    put("videoId", song_id)
                }
            }
        }
    }
}
