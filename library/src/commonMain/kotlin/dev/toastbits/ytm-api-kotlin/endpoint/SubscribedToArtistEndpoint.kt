package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi

abstract class SubscribedToArtistEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun isSubscribedToArtist(artist_id: String): Result<Boolean>
}
