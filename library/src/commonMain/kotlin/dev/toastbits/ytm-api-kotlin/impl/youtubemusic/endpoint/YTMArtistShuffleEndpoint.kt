package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.endpoint.ArtistShuffleEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.radio.YoutubeiNextContinuationResponse
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMArtistShuffleEndpoint(override val api: YoutubeMusicApi): ArtistShuffleEndpoint() {
    override suspend fun getArtistShuffle(
        artist_shuffle_playlist_id: String,
        continuation: String?
    ): Result<RadioData> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("next")
            addAuthApiHeaders()
            postWithBody {
                put("enablePersistentPlaylistPanel", true)
                put("playlistId", artist_shuffle_playlist_id)

                if (continuation != null) {
                    put("continuation", continuation)
                }
            }
        }

        val radio: YoutubeiNextResponse.PlaylistPanelRenderer?

        if (continuation == null) {
            val data: YoutubeiNextResponse = response.body()

            val renderer: YoutubeiNextResponse.MusicQueueRenderer = data
                .contents
                .singleColumnMusicWatchNextResultsRenderer
                .tabbedRenderer
                .watchNextTabbedResultsRenderer
                .tabs
                .first()
                .tabRenderer
                .content!!
                .musicQueueRenderer

            radio = renderer.content?.playlistPanelRenderer
        }
        else {
            val data: YoutubeiNextContinuationResponse = response.body()

            radio = data
                .continuationContents
                .playlistPanelContinuation
        }

        return@runCatching RadioData(
            radio?.contents?.map { item ->
                val renderer = item.getRenderer()

                val title: String = renderer.title.first_text
                val artist: Artist? = renderer.getArtist(api).getOrThrow()

                return@map Song(
                    id = renderer.videoId,
                    name = title,
                    artist = artist
                )
            } ?: emptyList(),
            radio?.continuations?.firstOrNull()?.data?.continuation
        )
    }
}
