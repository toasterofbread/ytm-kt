package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.SongLikedStatus

abstract class SongLikedEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun getSongLiked(song_id: String): Result<SongLikedStatus>
}
