package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist

abstract class AccountPlaylistsEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun getAccountPlaylists(): Result<List<YtmPlaylist>>
}

abstract class CreateAccountPlaylistEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun createAccountPlaylist(title: String, description: String): Result<String>
}

abstract class DeleteAccountPlaylistEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun deleteAccountPlaylist(playlist_id: String): Result<Unit>
}
