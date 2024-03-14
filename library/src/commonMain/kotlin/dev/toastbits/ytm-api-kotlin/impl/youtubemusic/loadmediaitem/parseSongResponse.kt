package dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem

import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.Song

suspend fun parseSongResponse(
    song_id: String,
    response: HttpResponse,
    api: YoutubeApi
): Result<Song> = runCatching {
    val response_data: YoutubeiNextResponse = response.body()
    val tabs: List<YoutubeiNextResponse.Tab> =
        response_data
            .contents
            .singleColumnMusicWatchNextResultsRenderer
            .tabbedRenderer
            .watchNextTabbedResultsRenderer
            .tabs

    val lyrics_browse_id: String? = tabs.getOrNull(1)?.tabRenderer?.endpoint?.browseEndpoint?.browseId
    val related_browse_id: String? = tabs.getOrNull(2)?.tabRenderer?.endpoint?.browseEndpoint?.browseId

    val video: YoutubeiNextResponse.PlaylistPanelVideoRenderer =
        tabs[0].tabRenderer.content!!.musicQueueRenderer.content!!.playlistPanelRenderer.contents.first().playlistPanelVideoRenderer!!

    val title: String = video.title.first_text
    val is_explicit: Boolean = video.badges?.any { it.isExplicit() } == true

    val artist: Artist? = video.getArtist(api).getOrThrow()

    return@runCatching Song(
        id = song_id,
        artist = artist,
        name = title,
        is_explicit = is_explicit,
        lyrics_browse_id = lyrics_browse_id,
        related_browse_id = related_browse_id
    )
}
