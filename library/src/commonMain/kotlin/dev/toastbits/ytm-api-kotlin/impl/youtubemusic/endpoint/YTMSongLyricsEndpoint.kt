package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.SongLyricsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse

class YTMSongLyricsEndpoint(override val api: YoutubeMusicApi): SongLyricsEndpoint() {
    private class LyricsBrowseResponse(val contents: YoutubeiBrowseResponse.Content)

    override suspend fun getSongLyrics(lyrics_id: String): Result<String?> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(
                mapOf(
                    "browseId" to lyrics_id
                )
            )
        }

        val data: LyricsBrowseResponse = response.body

        val lyrics_text: String? =
            data.contents.sectionListRenderer?.contents?.firstOrNull()?.musicDescriptionShelfRenderer?.description?.firstTextOrNull()

        return@runCatching lyrics_text
    }
}
