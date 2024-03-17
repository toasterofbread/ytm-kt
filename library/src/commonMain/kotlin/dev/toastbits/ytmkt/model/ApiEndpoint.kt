package dev.toastbits.ytmkt.model

import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

abstract class ApiEndpoint: ApiImplementable {
    abstract val api: YtmApi
    override fun getNotImplementedMessage(): String = "Endpoint not implemented:\n${getIdentifier()}"

    fun HttpRequestBuilder.endpointPath(path: String) =
        with (api) {
            endpointPath(path)
        }

    open suspend fun HttpRequestBuilder.addApiHeadersWithAuthenticated(include: List<String>? = null) =
        with (api) {
            addAuthenticatedApiHeaders(include)
        }

    open suspend fun HttpRequestBuilder.addApiHeadersWithoutAuthentication(include: List<String>? = null) =
        with (api) {
            addUnauthenticatedApiHeaders(include)
        }

    suspend fun HttpRequestBuilder.postWithBody(
        base: JsonObject? = null,
        buildPostBody: (JsonObjectBuilder.() -> Unit)? = null
    ) = with (api) {
        postWithBody(base, buildPostBody)
    }
}
