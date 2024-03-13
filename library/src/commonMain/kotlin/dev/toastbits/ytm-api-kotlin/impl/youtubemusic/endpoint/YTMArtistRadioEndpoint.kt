package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.endpoint.ArtistShuffleEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.radio.YoutubeiNextContinuationResponse
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

class YTMArtistShuffleEndpoint(override val api: YoutubeMusicApi): ArtistShuffleEndpoint() {
    override suspend fun getArtistShuffle(
        artist: Artist,
        continuation: String?
    ): Result<RadioData> = runCatching {
        artist.loadData(api.context, populate_data = false).onFailure {
            return@runCatching Result.failure(RuntimeException(it))
        }

        val shuffle_playlist_id: String? = artist.ShufflePlaylistId.get(api.database)
            ?: return@runCatching Result.failure(RuntimeException("ShufflePlaylistId not loaded for artist $artist"))

        val response: HttpResponse = api.client.request {
            endpointPath("next")
            addAuthApiHeaders()
            postWithBody(
                mutableMapOf(
                    "enablePersistentPlaylistPanel" to true,
                    "playlistId" to shuffle_playlist_id
                )
                .also {
                    if (continuation != null) {
                        it["continuation"] = continuation
                    }
                }
            )
        }

        val radio: YoutubeiNextResponse.PlaylistPanelRenderer?

        if (continuation == null) {
            val data: YoutubeiNextResponse = response.body

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
            val data: YoutubeiNextContinuationResponse = response.body

            radio = data
                .continuationContents
                .playlistPanelContinuation
        }

        return@runCatching RadioData(
            radio?.contents?.map { item ->
                val renderer = item.getRenderer()

                val title: String = renderer.title.first_text
                val artist: Artist? = renderer.getArtist(song, api.context).getOrThrow()

                return@map Song(
                    id = renderer.videoId,
                    title = title,
                    artist_id = artist?.id
                )
            } ?: emptyList(),
            radio?.continuations?.firstOrNull()?.data?.continuation
        )
    }
}
