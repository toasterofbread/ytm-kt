package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.SongLikedStatus
import dev.toastbits.ytmapi.YoutubeApi

abstract class SetSongLikedEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun setSongLiked(song_id: String, liked: SongLikedStatus): Result<Unit>
}
