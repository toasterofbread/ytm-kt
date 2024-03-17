package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.SongLikedStatus

abstract class SetSongLikedEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun setSongLiked(song_id: String, liked: SongLikedStatus): Result<Unit>
}
