package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist

abstract class AccountPlaylistsEndpoint: AuthenticatedApiEndpoint() {

    /**
     * Loads the authenticated user's owned playlists.
     *
     * @return the list of playlists owned by the authenticated user.
     */
    abstract suspend fun getAccountPlaylists(): Result<List<YtmPlaylist>>
}

abstract class CreateAccountPlaylistEndpoint: AuthenticatedApiEndpoint() {

    /**
     * Creates a new playlist with the passed [title] and [description].
     *
     * @param title The title of the new playlist.
     * @param description The description of the new playlist.
     * @return the ID of the created playlist.
     */
    abstract suspend fun createAccountPlaylist(title: String, description: String): Result<String>
}

abstract class DeleteAccountPlaylistEndpoint: AuthenticatedApiEndpoint() {

    /**
     * Deletes the user-owned playlist with passed ID.
     *
     * @param playlist_id The ID of the playlist to delete. Must be owned by the authenticated user.
     */
    abstract suspend fun deleteAccountPlaylist(playlist_id: String): Result<Unit>
}
