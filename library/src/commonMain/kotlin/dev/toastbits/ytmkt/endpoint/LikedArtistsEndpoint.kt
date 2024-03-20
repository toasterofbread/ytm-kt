package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist

abstract class LikedArtistsEndpoint: AuthenticatedApiEndpoint() {

    /**
     * Loads the authenticated user's liked artists.
     *
     * @return the list of liked artists.
     */
    abstract suspend fun getLikedArtists(): Result<List<YtmArtist>>
}
