package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongLikedStatus
import dev.toastbits.ytmapi.model.external.mediaitem.song.toLong
import dev.toastbits.ytmapi.endpoint.SetSongLikedEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo

class YTMSetSongLikedEndpoint(override val auth: YoutubeMusicAuthInfo): SetSongLikedEndpoint() {
    override suspend fun setSongLiked(
        song: Song, 
        liked: SongLikedStatus
    ): Result<Unit> = runCatching {
        api.client.request {
            endpointPath("" + when (liked) {
                SongLikedStatus.NEUTRAL -> "like/removelike"
                SongLikedStatus.LIKED -> "like/like"
                SongLikedStatus.DISLIKED -> "like/dislike"
            })
            addAuthApiHeaders()
            postWithBody(
                mapOf("target" to mapOf("videoId" to song.id))
            )
        }
    }
}
