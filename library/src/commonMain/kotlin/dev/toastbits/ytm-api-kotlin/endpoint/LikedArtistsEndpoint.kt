package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi

abstract class LikedArtistsEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun getLikedArtists(): Result<List<ArtistData>>
}
