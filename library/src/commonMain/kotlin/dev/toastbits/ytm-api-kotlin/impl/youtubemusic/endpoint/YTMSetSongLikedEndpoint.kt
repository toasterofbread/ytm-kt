package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.SongLikedStatus
import dev.toastbits.ytmapi.endpoint.SetSongLikedEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import io.ktor.client.request.request
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.put

class YTMSetSongLikedEndpoint(override val auth: YoutubeMusicAuthInfo): SetSongLikedEndpoint() {
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
            addAuthApiHeaders()
            postWithBody {
                putJsonObject("target") {
                    put("videoId", song_id)
                }
            }
        }
    }
}
