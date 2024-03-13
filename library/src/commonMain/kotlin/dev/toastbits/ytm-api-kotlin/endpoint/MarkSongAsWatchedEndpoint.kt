package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.YoutubeApi

abstract class MarkSongAsWatchedEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun markSongAsWatched(song: Song): Result<Unit>
}
