package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint

abstract class MarkSongAsWatchedEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun markSongAsWatched(song_id: String): Result<Unit>
}
