package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint

abstract class SubscribedToArtistEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun isSubscribedToArtist(artist_id: String): Result<Boolean>
}
