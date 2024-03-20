package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist

abstract class LikedPlaylistsEndpoint: AuthenticatedApiEndpoint() {
    
    /**
     * Loads the authenticated user's liked playlists.
     *
     * @return the list of liked playlists.
     */
    abstract suspend fun getLikedPlaylists(): Result<List<YtmPlaylist>>
}
