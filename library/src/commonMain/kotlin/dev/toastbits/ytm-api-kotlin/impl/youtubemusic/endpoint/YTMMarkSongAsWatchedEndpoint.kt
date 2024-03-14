package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.MarkSongAsWatchedEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.expectSuccess
import kotlinx.serialization.json.put
import kotlin.random.Random

private data class PlaybackTrackingRepsonse(
    val playbackTracking: PlaybackTracking
) {
    val playback_url: String get() = playbackTracking.videostatsPlaybackUrl.baseUrl

    data class PlaybackTracking(
        val videostatsPlaybackUrl: TrackingUrl
    )
    data class TrackingUrl(val baseUrl: String)
}

private const val CPN_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
private fun generateCpn(): String {
    return (0 until 16).map { CPN_ALPHABET[Random.nextInt(0, 64)] }.joinToString("")
}

class YTMMarkSongAsWatchedEndpoint(override val auth: YoutubeMusicAuthInfo): MarkSongAsWatchedEndpoint() {
    override suspend fun markSongAsWatched(song: Song): Result<Unit> = runCatching {
        var response: HttpResponse = api.client.request {
            buildPlayerRequest(song.id, false)
        }

        if (response.status.value !in 200 .. 299) {
            response = api.client.request {
                buildPlayerRequest(song.id, true)
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
            addAuthApiHeaders(include = listOf("cookie", "user-agent"))
        }
    }

    private suspend fun HttpRequestBuilder.buildPlayerRequest(id: String, alt: Boolean) {
        endpointPath("player")
        addAuthApiHeaders()
        postWithBody(
            if (alt) YoutubeApi.PostBodyContext.ANDROID_MUSIC else YoutubeApi.PostBodyContext.BASE
        ) {
            put("videoId", id)
        }

        if (!alt) {
            expectSuccess = false
        }
    }
}
