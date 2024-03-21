package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.ApiEndpoint
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import kotlinx.serialization.Serializable

open class YTMGetVisitorIdEndpoint(override val api: YoutubeiApi): ApiEndpoint() {
    suspend fun getVisitorId(): Result<String> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("visitor_id")

            with (api) {
                addUnauthenticatedApiHeaders(add_visitor_id = false)
            }
            postWithBody()
        }

        val data: VisitorIdResponse = response.body()
        return@runCatching data.responseContext.visitorData
    }

    @Serializable
    private data class VisitorIdResponse(val responseContext: ResponseContext) {
        @Serializable
        data class ResponseContext(val visitorData: String)
    }
}
