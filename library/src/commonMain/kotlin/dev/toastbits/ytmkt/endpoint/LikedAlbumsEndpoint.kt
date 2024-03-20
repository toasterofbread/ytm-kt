package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist

abstract class LikedAlbumsEndpoint: AuthenticatedApiEndpoint() {

    /**
     * Loads the authenticated user's liked albums.
     *
     * @return the list of liked albums.
     */
    abstract suspend fun getLikedAlbums(): Result<List<YtmPlaylist>>
}
