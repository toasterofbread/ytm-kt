package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist

abstract class LikedAlbumsEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun getLikedAlbums(): Result<List<YtmPlaylist>>
}
