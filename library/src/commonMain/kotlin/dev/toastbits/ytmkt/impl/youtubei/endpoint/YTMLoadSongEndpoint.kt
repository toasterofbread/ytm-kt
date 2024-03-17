package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.endpoint.LoadSongEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.loadmediaitem.parseSongResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable

open class YTMLoadSongEndpoint(override val api: YoutubeiApi): LoadSongEndpoint() {
    override suspend fun loadSong(song_id: String): Result<YtmSong> = runCatching {
        val next_response: HttpResponse = api.client.request {
            endpointPath("next")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("enablePersistentPlaylistPanel", true)
                put("isAudioOnly", true)
                put("videoId", song_id)
            }
        }

        val song: YtmSong? = parseSongResponse(song_id, next_response, api).getOrNull()
        if (song != null) {
            return@runCatching song
        }

        val player_response: HttpResponse = api.client.request {
            endpointPath("player")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("videoId",song_id)
            }
        }

        val video_data: PlayerData = player_response.body()
        val video_details = video_data.videoDetails!!

        return@runCatching YtmSong(
            song_id,
            name = video_details.title,
            artist = YtmArtist(video_details.channelId)
        )
    }
}

@Serializable
private data class PlayerData(
    val videoDetails: VideoDetails?,
) {
    @Serializable
    class VideoDetails(
        val title: String,
        val channelId: String,
    )
}

