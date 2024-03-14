package dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.ArtistLayout
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.model.external.MediaItemYoutubePage
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.PlaylistBuilder
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.uistrings.YoutubeUiString
import dev.toastbits.ytmapi.uistrings.parseYoutubeDurationString
import dev.toastbits.ytmapi.uistrings.parseYoutubeSubscribersString
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.internal.Header
import dev.toastbits.ytmapi.model.internal.HeaderRenderer
import dev.toastbits.ytmapi.model.internal.TextRun
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse
import dev.toastbits.ytmapi.radio.RadioContinuation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body

suspend fun parsePlaylistResponse(
    playlist_id: String,
    response: HttpResponse,
    hl: String,
    api: YoutubeApi,
    is_radio: Boolean = false
): Result<Playlist> = runCatching {
    val parsed: YoutubeiBrowseResponse = response.body()

    // Skip unneeded information for radios
    if (is_radio) {
        val playlist_shelf = parsed
            .contents!!
            .singleColumnBrowseResultsRenderer!!
            .tabs[0]
            .tabRenderer
            .content!!
            .sectionListRenderer!!
            .contents!![0]
            .musicPlaylistShelfRenderer!!

        val items: List<Song> = playlist_shelf.contents!!.mapNotNull { data ->
            val data_item: MediaItem? = data.toMediaItemData(hl, api)?.first
            return@mapNotNull if (data_item is Song) data_item else null
        }

        val continuation: RadioContinuation? =
            playlist_shelf.continuations?.firstOrNull()?.nextRadioContinuationData?.continuation?.let {
                RadioContinuation(it, RadioContinuation.Type.SONG, playlist_id)
            }

        val thumbnail_provider: ThumbnailProvider? =
            parsed.header?.getRenderer()?.let { renderer ->
                ThumbnailProvider.fromThumbnails(renderer.getThumbnails())
            }

        return@runCatching Playlist(
            id = playlist_id,
            items = items,
            continuation = continuation,
            thumbnail_provider = thumbnail_provider
        )
    }

    val builder: PlaylistBuilder = PlaylistBuilder(playlist_id)

    val header_renderer: HeaderRenderer? = parsed.header?.getRenderer()
    if (header_renderer != null) {
        builder.name = header_renderer.title!!.first_text
        builder.thumbnail_provider = ThumbnailProvider.fromThumbnails(header_renderer.getThumbnails())

        header_renderer.subtitle?.runs?.also { subtitle ->
            val artist_run: TextRun? = subtitle.firstOrNull {
                it.navigationEndpoint?.browseEndpoint?.let { endpoint ->
                    endpoint.browseId != null && endpoint.getMediaItemType() == MediaItem.Type.ARTIST
                } ?: false
            }

            if (artist_run != null) {
                builder.artist = Artist(
                    id = artist_run.navigationEndpoint!!.browseEndpoint!!.browseId!!,
                    name = artist_run.text
                )
            }

            builder.year = subtitle.lastOrNull { last_run ->
                last_run.text.all { it.isDigit() }
            }?.text?.toInt()
        }

        header_renderer.secondSubtitle?.runs?.also { second_subtitle ->
            for (run in second_subtitle.reversed().withIndex()) {
                when (run.index) {
                    0 -> builder.total_duration = parseYoutubeDurationString(run.value.text, hl)
                    1 -> builder.item_count = run.value.text.filter { it.isDigit() }.toInt()
                }
            }
        }
    }

    val menu_buttons: List<Header.TopLevelButton>? =
        parsed.header?.musicDetailHeaderRenderer?.menu?.menuRenderer?.topLevelButtons

    if (menu_buttons?.any { it.buttonRenderer?.icon?.iconType == "EDIT" } == true) {
        builder.owner_id = api.user_auth_state?.own_channel_id
        builder.type = Playlist.Type.PLAYLIST
    }

    val section_list_renderer: YoutubeiBrowseResponse.SectionListRenderer? = with (parsed.contents!!) {
        if (singleColumnBrowseResultsRenderer != null) {
            singleColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer
        }
        else {
            twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
        }
    }

    for (row in section_list_renderer?.contents.orEmpty().withIndex()) {
        if (row.value.description != null) {
            continue
        }

        val row_items = row.value.getMediaItemsAndSetIds(hl, api)
        builder.items = row_items.map { it.first }.filterIsInstance<Song>()

        val continuation_token: String? =
            row.value.musicPlaylistShelfRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation

        builder.continuation = continuation_token?.let {
            RadioContinuation(it, RadioContinuation.Type.PLAYLIST)
        }
        builder.item_set_ids = if (row_items.all { it.second != null }) row_items.map { it.second!! } else null

        // Playlists don't display indices
        if (row.value.musicShelfRenderer?.contents?.firstOrNull()?.musicResponsiveListItemRenderer?.index != null) {
            builder.type = Playlist.Type.ALBUM
        }
    }

    return@runCatching builder.build()
}
