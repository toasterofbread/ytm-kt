package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint

abstract class MarkSongAsWatchedEndpoint: AuthenticatedApiEndpoint() {
    
    /**
     * Marks the passed song as watched on the authenticated user's account.
     *
     * @param song_id The ID of the song to mark as watched.
     */
    abstract suspend fun markSongAsWatched(song_id: String): Result<Unit>
}
