package dev.toastbits.ytmkt.model

import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

abstract class ApiEndpoint: ApiImplementable {
    abstract val api: YtmApi
    override fun getNotImplementedMessage(): String = "Endpoint not implemented:\n${getIdentifier()}"

    fun HttpRequestBuilder.endpointPath(path: String, non_music_api: Boolean = false) =
        with (api) {
            endpointPath(path, non_music_api)
        }

    open suspend fun HttpRequestBuilder.addApiHeadersWithAuthenticated(include: List<String>? = null, non_music_api: Boolean = false) =
        with (api) {
            addAuthenticatedApiHeaders(include, non_music_api = non_music_api)
        }

    open suspend fun HttpRequestBuilder.addApiHeadersWithoutAuthentication(include: List<String>? = null, non_music_api: Boolean = false) =
        with (api) {
            addUnauthenticatedApiHeaders(include, non_music_api = non_music_api)
        }

    suspend fun HttpRequestBuilder.postWithBody(
        base: JsonObject? = null,
        buildPostBody: (JsonObjectBuilder.() -> Unit)? = null
    ) = with (api) {
        postWithBody(base, buildPostBody)
    }
}
