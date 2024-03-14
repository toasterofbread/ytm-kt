package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.SubscribedToArtistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMSubscribedToArtistEndpoint(override val auth: YoutubeMusicAuthInfo): SubscribedToArtistEndpoint() {
    override suspend fun isSubscribedToArtist(artist_id: String): Result<Boolean> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", artist_id)
            }
        }

        val parsed: ArtistBrowseResponse = response.body()
        return@runCatching parsed.getSubscribed() == true
    }
}

private class ArtistBrowseResponse(val header: Header) {
    class Header(val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer?)
    class MusicImmersiveHeaderRenderer(val subscriptionButton: SubscriptionButton)
    class SubscriptionButton(val subscribeButtonRenderer: SubscribeButtonRenderer)
    class SubscribeButtonRenderer(val subscribed: Boolean)

    fun getSubscribed(): Boolean? = header.musicImmersiveHeaderRenderer?.subscriptionButton?.subscribeButtonRenderer?.subscribed
}
