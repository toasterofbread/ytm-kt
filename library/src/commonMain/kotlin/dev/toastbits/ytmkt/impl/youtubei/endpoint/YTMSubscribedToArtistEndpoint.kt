package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.SubscribedToArtistEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable

open class YTMSubscribedToArtistEndpoint(override val auth: YoutubeiAuthenticationState): SubscribedToArtistEndpoint() {
    override suspend fun isSubscribedToArtist(artist_id: String): Result<Boolean> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("browseId", artist_id)
            }
        }

        val parsed: ArtistBrowseResponse = response.body()
        return@runCatching parsed.getSubscribed() == true
    }
}

@Serializable
private class ArtistBrowseResponse(val header: Header) {
    @Serializable
    class Header(val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer?)
    @Serializable
    class MusicImmersiveHeaderRenderer(val subscriptionButton: SubscriptionButton)
    @Serializable
    class SubscriptionButton(val subscribeButtonRenderer: SubscribeButtonRenderer)
    @Serializable
    class SubscribeButtonRenderer(val subscribed: Boolean)

    fun getSubscribed(): Boolean? = header.musicImmersiveHeaderRenderer?.subscriptionButton?.subscribeButtonRenderer?.subscribed
}
