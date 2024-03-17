package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.SetSubscribedToArtistEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import io.ktor.client.request.request
import kotlinx.serialization.json.add
import kotlinx.serialization.json.putJsonArray

open class YTMSetSubscribedToArtistEndpoint(override val auth: YoutubeiAuthenticationState): SetSubscribedToArtistEndpoint() {
    override suspend fun setSubscribedToArtist(
        artist_id: String,
        subscribed: Boolean,
        subscribe_channel_id: String?
    ): Result<Unit> = runCatching {
        api.client.request {
            endpointPath("subscription/${if (subscribed) "subscribe" else "unsubscribe"}")
            addApiHeadersWithAuthenticated()
            postWithBody {
                putJsonArray("channelIds") {
                    add(subscribe_channel_id ?: artist_id)
                }
            }
        }
    }
}
