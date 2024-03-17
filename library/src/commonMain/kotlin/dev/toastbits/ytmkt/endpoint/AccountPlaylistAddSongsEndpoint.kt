package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint

abstract class AccountPlaylistAddSongsEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun addSongs(playlist_id: String, song_ids: Collection<String>): Result<Unit>
}
