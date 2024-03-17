package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint

abstract class SetSubscribedToArtistEndpoint: AuthenticatedApiEndpoint() {
    abstract suspend fun setSubscribedToArtist(
        artist_id: String, 
        subscribed: Boolean,
        subscribe_channel_id: String? = null
    ): Result<Unit>
}
