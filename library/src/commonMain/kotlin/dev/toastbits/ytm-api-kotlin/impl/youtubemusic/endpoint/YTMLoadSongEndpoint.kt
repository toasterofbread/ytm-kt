package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.artist.ArtistRef
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.endpoint.LoadSongEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.processDefaultResponse

private data class PlayerData(
    val videoDetails: VideoDetails?,
) {
    class VideoDetails(
        val title: String,
        val channelId: String,
    )
}

class YTMLoadSongEndpoint(override val api: YoutubeMusicApi): LoadSongEndpoint() {
    override suspend fun loadSong(song_id: String): Result<SongData> = runCatching {
        val hl: String = api.data_language
        val next_response: HttpResponse = api.client.request {
            endpointPath("next")
            addAuthApiHeaders()
            postWithBody(
                mapOf(
                    "enablePersistentPlaylistPanel" to true,
                    "isAudioOnly" to true,
                    "videoId" to song_id
                )
            )
        }

        val next_result: Result<Unit> = processDefaultResponse(song_data, next_response, hl, api)

        if (next_result.isFailure) {
            // 'next' endpoint has no artist, use 'player' instead
            val player_response: HttpResponse = api.client.request {
                endpointPath("player")
                addAuthApiHeaders()
                postWithBody(
                    mapOf("videoId" to song_id)
                )
            }

            val video_data: PlayerData = player_response.body
            video_data.videoDetails?.also { details ->
                song_data.title = details.title
                song_data.artist_id = details
            }
        }

        return@runCatching song_data
    }
}
