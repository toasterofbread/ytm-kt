package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.SongLyricsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMSongLyricsEndpoint(override val api: YoutubeMusicApi): SongLyricsEndpoint() {
    private class LyricsBrowseResponse(val contents: YoutubeiBrowseResponse.Content)

    override suspend fun getSongLyrics(lyrics_id: String): Result<String> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", lyrics_id)
            }
        }

        val data: LyricsBrowseResponse = response.body()
        return@runCatching data
            .contents
            .sectionListRenderer!!
            .contents!!
            .first()
            .musicDescriptionShelfRenderer!!
            .description
            .first_text
    }
}
