package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.ArtistShuffleEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.radio.BuiltInRadioContinuation
import dev.toastbits.ytmkt.radio.BuiltInRadioContinuation.Type
import dev.toastbits.ytmkt.radio.YoutubeiNextContinuationResponse
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

open class YTMArtistShuffleEndpoint(override val api: YoutubeiApi): ArtistShuffleEndpoint() {
    override suspend fun getArtistShuffle(
        artist_shuffle_playlist_id: String,
        continuation: String?
    ): Result<RadioData> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("next")
            addApiHeadersWithAuthenticated()
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
                val artists: List<YtmArtist>? = renderer.getArtists(api).getOrThrow()

                return@map YtmSong(
                    id = YtmSong.cleanId(renderer.videoId),
                    name = title,
                    artists = artists
                )
            } ?: emptyList(),
            radio?.continuations?.firstOrNull()?.data?.continuation?.let { continuation ->
                BuiltInRadioContinuation(continuation, Type.ARTIST_SHUFFLE, artist_shuffle_playlist_id)
            }
        )
    }
}
