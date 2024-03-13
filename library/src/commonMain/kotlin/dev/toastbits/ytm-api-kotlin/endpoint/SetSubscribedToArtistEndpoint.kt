package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.YoutubeApi

abstract class SetSubscribedToArtistEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun setSubscribedToArtist(
        artist_id: String, 
        subscribed: Boolean,
        subscribe_channel_id: String? = null
    ): Result<Unit>
}
