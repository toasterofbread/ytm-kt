package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.SongLyricsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable

open class YTMSongLyricsEndpoint(override val api: YoutubeiApi): SongLyricsEndpoint() {
    @Serializable
    private class LyricsBrowseResponse(val contents: YoutubeiBrowseResponse.Content)

    override suspend fun getSongLyrics(lyrics_id: String): Result<String> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
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
