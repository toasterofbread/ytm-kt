package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.SetSubscribedToArtistEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import io.ktor.client.request.request
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add
import kotlinx.serialization.json.putJsonArray

class YTMSetSubscribedToArtistEndpoint(override val auth: YoutubeMusicAuthInfo): SetSubscribedToArtistEndpoint() {
    override suspend fun setSubscribedToArtist(
        artist_id: String,
        subscribed: Boolean,
        subscribe_channel_id: String?
    ): Result<Unit> = runCatching {
        api.client.request {
            endpointPath("subscription/${if (subscribed) "subscribe" else "unsubscribe"}")
            addAuthApiHeaders()
            postWithBody {
                putJsonArray("channelIds") {
                    add(subscribe_channel_id ?: artist_id)
                }
            }
        }
    }
}
