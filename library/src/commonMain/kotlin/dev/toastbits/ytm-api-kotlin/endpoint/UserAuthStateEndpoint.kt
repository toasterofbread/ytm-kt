package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi
import io.ktor.http.Headers

abstract class UserAuthStateEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun byHeaders(headers: Headers): Result<YoutubeApi.UserAuthState>
}
