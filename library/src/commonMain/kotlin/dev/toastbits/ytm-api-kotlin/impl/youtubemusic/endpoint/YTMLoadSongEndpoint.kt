package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.endpoint.LoadSongEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem.parseSongResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

private data class PlayerData(
    val videoDetails: VideoDetails?,
) {
    class VideoDetails(
        val title: String,
        val channelId: String,
    )
}

class YTMLoadSongEndpoint(override val api: YoutubeMusicApi): LoadSongEndpoint() {
    override suspend fun loadSong(song_id: String): Result<Song> = runCatching {
        val next_response: HttpResponse = api.client.request {
            endpointPath("next")
            addAuthApiHeaders()
            postWithBody {
                put("enablePersistentPlaylistPanel", true)
                put("isAudioOnly", true)
                put("videoId", song_id)
            }
        }

        val song: Song? = parseSongResponse(song_id, next_response, api).getOrNull()
        if (song != null) {
            return@runCatching song
        }

        val player_response: HttpResponse = api.client.request {
            endpointPath("player")
            addAuthApiHeaders()
            postWithBody {
                put("videoId",song_id)
            }
        }

        val video_data: PlayerData = player_response.body()
        val video_details = video_data.videoDetails!!

        return@runCatching Song(
            song_id,
            name = video_details.title,
            artist = Artist(video_details.channelId)
        )
    }
}
