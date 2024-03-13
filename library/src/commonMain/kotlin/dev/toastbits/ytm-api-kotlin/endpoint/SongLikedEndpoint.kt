package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.song.SongLikedStatus
import dev.toastbits.ytmapi.YoutubeApi

abstract class SongLikedEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun getSongLiked(song_id: String): Result<SongLikedStatus>
}
