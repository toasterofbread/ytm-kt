package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi

abstract class AccountPlaylistAddSongsEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun addSongs(playlist_id: String, song_ids: Collection<String>): Result<Unit>
}
