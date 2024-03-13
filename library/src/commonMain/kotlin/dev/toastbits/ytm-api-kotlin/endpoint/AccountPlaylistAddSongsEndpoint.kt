package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylist
import dev.toastbits.ytmapi.YoutubeApi

abstract class AccountPlaylistAddSongsEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun addSongs(playlist: RemotePlaylist, song_ids: Collection<String>): Result<Unit>
}
