package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint

abstract class AccountPlaylistAddSongsEndpoint: AuthenticatedApiEndpoint() {

    /**
     * Appends songs in [song_ids] to the playlist with ID [playlist_id].
     *
     * @param playlist_id The ID of the playlist to add songs to. Must be editable by the authenticated user.
     * @param song_ids An ordered collection of song IDs to append to the playlist.
     */
    abstract suspend fun addSongs(playlist_id: String, song_ids: Collection<String>): Result<Unit>
}
