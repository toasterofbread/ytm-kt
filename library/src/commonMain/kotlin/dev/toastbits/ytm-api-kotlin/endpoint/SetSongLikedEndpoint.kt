package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongLikedStatus
import dev.toastbits.ytmapi.YoutubeApi

abstract class SetSongLikedEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun setSongLiked(song: Song, liked: SongLikedStatus): Result<Unit>
}
