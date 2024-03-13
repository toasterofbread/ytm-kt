package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.YoutubeApi

abstract class AccountPlaylistsEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun getAccountPlaylists(): Result<List<Playlist>>
}

abstract class CreateAccountPlaylistEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun createAccountPlaylist(title: String, description: String): Result<String>
}

abstract class DeleteAccountPlaylistEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun deleteAccountPlaylist(playlist_id: String): Result<Unit>
}
