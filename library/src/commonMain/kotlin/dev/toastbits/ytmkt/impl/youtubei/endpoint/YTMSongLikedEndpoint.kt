package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.SongLikedStatus
import dev.toastbits.ytmkt.endpoint.SongLikedEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

private data class PlayerLikeResponse(
    val playerOverlays: PlayerOverlays?
) {
    val status: LikeButtonRenderer? get() = playerOverlays?.playerOverlayRenderer?.actions?.single()?.likeButtonRenderer

    class PlayerOverlays(val playerOverlayRenderer: PlayerOverlayRenderer?)
    data class PlayerOverlayRenderer(val actions: List<Action>?)
    data class Action(val likeButtonRenderer: LikeButtonRenderer?)
    data class LikeButtonRenderer(val likeStatus: String, val likesAllowed: Boolean)
}

open class YTMSongLikedEndpoint(override val auth: YoutubeiAuthenticationState): SongLikedEndpoint() {
    override suspend fun getSongLiked(song_id: String): Result<SongLikedStatus> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("next")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("videoId", song_id)
            }
        }

        val data: PlayerLikeResponse = response.body()

        if (data.status == null) {
            throw NullPointerException(song_id)
        }

        return@runCatching when (data.status!!.likeStatus) {
            "LIKE" -> SongLikedStatus.LIKED
            "DISLIKE" -> SongLikedStatus.DISLIKED
            "INDIFFERENT" -> SongLikedStatus.NEUTRAL
            else -> throw NotImplementedError("$song_id (${data.status!!.likeStatus})")
        }
    }
}
