package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist

abstract class LikedPlaylistsEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun getLikedPlaylists(): Result<List<YtmPlaylist>>
}
