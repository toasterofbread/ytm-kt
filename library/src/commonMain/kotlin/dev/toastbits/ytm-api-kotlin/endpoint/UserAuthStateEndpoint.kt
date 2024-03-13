package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi

abstract class UserAuthStateEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun byHeaders(headers: Headers): Result<YoutubeApi.UserAuthState>
}
