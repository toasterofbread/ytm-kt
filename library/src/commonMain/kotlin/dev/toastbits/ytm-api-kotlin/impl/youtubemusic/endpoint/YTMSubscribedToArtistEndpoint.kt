package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.SubscribedToArtistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo

class YTMSubscribedToArtistEndpoint(override val auth: YoutubeMusicAuthInfo): SubscribedToArtistEndpoint() {
    override suspend fun isSubscribedToArtist(artist_id: String): Result<Boolean> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(
                mapOf("browseId" to artist.id)
            )
        }

        val parsed: ArtistBrowseResponse = response.body
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
