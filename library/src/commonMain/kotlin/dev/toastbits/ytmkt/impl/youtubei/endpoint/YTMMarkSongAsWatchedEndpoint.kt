package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.endpoint.MarkSongAsWatchedEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.expectSuccess
import kotlinx.serialization.json.put
import kotlin.random.Random
import kotlinx.serialization.Serializable

@Serializable
private data class PlaybackTrackingRepsonse(
    val playbackTracking: PlaybackTracking
) {
    val playback_url: String get() = playbackTracking.videostatsPlaybackUrl.baseUrl

    @Serializable
    data class PlaybackTracking(
        val videostatsPlaybackUrl: TrackingUrl
    )
    @Serializable
    data class TrackingUrl(val baseUrl: String)
}

private const val CPN_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
private fun generateCpn(): String {
    return (0 until 16).map { CPN_ALPHABET[Random.nextInt(0, 64)] }.joinToString("")
}

open class YTMMarkSongAsWatchedEndpoint(override val auth: YoutubeiAuthenticationState): MarkSongAsWatchedEndpoint() {
    override val api: YoutubeiApi get() = auth.api

    override suspend fun markSongAsWatched(song_id: String): Result<Unit> = runCatching {
        var response: HttpResponse = api.client.request {
            buildPlayerRequest(song_id, false)
        }

        if (response.status.value !in 200 .. 299) {
            response = api.client.request {
                buildPlayerRequest(song_id, true)
            }
        }

        val data: PlaybackTrackingRepsonse = response.body()

        check(data.playback_url.contains("s.youtube.com")) { data.playback_url }

        val playback_url: String = data.playback_url.replace("s.youtube.com", "music.youtube.com")

        api.client.request(playback_url) {
            url {
                parameters.append("ver", "2")
                parameters.append("c", "WEB_REMIX")
                parameters.append("cpn", generateCpn())
            }
            addApiHeadersWithAuthenticated(include = listOf("cookie", "user-agent"))
        }
    }

    private suspend fun HttpRequestBuilder.buildPlayerRequest(id: String, alt: Boolean) {
        endpointPath("player")
        addApiHeadersWithAuthenticated()
        postWithBody(
            (
                if (alt) YoutubeiPostBody.ANDROID_MUSIC
                else YoutubeiPostBody.BASE
            ).getPostBody(api)
        ) {
            put("videoId", id)
        }

        if (!alt) {
            expectSuccess = false
        }
    }
}
