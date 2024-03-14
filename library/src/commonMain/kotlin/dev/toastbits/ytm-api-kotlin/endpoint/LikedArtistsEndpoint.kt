package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.external.mediaitem.Artist

abstract class LikedArtistsEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract suspend fun getLikedArtists(): Result<List<Artist>>
}
