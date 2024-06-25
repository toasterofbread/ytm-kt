package dev.toastbits.ytmkt.impl.youtubei.loadmediaitem

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylistBuilder
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.uistrings.parseYoutubeDurationString
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.internal.Header
import dev.toastbits.ytmkt.model.internal.HeaderRenderer
import dev.toastbits.ytmkt.model.internal.TextRun
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.model.internal.YoutubeiShelf
import dev.toastbits.ytmkt.radio.RadioContinuation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body

suspend fun parsePlaylistResponse(
    playlist_id: String,
    response: HttpResponse,
    hl: String,
    api: YtmApi,
    is_radio: Boolean = false
): Result<YtmPlaylist> = runCatching {
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

        val items: List<YtmSong> = playlist_shelf.contents!!.mapNotNull { data ->
            val data_item: YtmMediaItem? = data.toMediaItemData(hl, api)?.first
            return@mapNotNull if (data_item is YtmSong) data_item else null
        }

        val continuation: RadioContinuation? =
            playlist_shelf.continuations?.firstOrNull()?.nextRadioContinuationData?.continuation?.let {
                RadioContinuation(it, RadioContinuation.Type.SONG, playlist_id)
            }

        val thumbnail_provider: ThumbnailProvider? =
            parsed.header?.getRenderer()?.let { renderer ->
                ThumbnailProvider.fromThumbnails(renderer.getThumbnails())
            }

        return@runCatching YtmPlaylist(
            id = playlist_id,
            items = items,
            continuation = continuation,
            thumbnail_provider = thumbnail_provider
        )
    }

    val builder: YtmPlaylistBuilder = YtmPlaylistBuilder(playlist_id)

    val header_renderer: HeaderRenderer? = parsed.header?.getRenderer()
    if (header_renderer != null) {
        builder.name = header_renderer.title!!.first_text
        builder.description = header_renderer.description?.first_text
        builder.thumbnail_provider = ThumbnailProvider.fromThumbnails(header_renderer.getThumbnails())

        for (run in header_renderer.subtitle?.runs.orEmpty()) {
            val browse_endpoint = run.navigationEndpoint?.browseEndpoint
            if (browse_endpoint?.browseId == null) {
                if (run.text.all { it.isDigit() }) {
                    builder.year = run.text.toInt()
                }
                continue
            }
            else if (browse_endpoint.getMediaItemType() != YtmMediaItem.Type.ARTIST) {
                continue
            }

            builder.artists = builder.artists.orEmpty().plus(
                YtmArtist(
                    id = browse_endpoint.browseId,
                    name = run.text
                )
            ).distinctBy { it.id }
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
        builder.type = YtmPlaylist.Type.PLAYLIST
    }

    val rows: List<YoutubeiShelf>? =
        parsed.contents?.let { contents ->
            val tabs: List<YoutubeiBrowseResponse.Tab>? = contents.singleColumnBrowseResultsRenderer?.tabs ?: contents.twoColumnBrowseResultsRenderer?.tabs

            return@let (
                tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents.orEmpty()
                + contents.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.contents.orEmpty()
            )
        }

    for (row in rows.orEmpty().withIndex()) {
        val description: String? = row.value.description
        if (description != null) {
            builder.description = description

            if (builder.items != null) {
                break
            }
            continue
        }

        val row_items = row.value.getMediaItemsAndSetIds(hl, api)
        builder.items = builder.items.orEmpty() + row_items.map { it.first }.filterIsInstance<YtmSong>()

        val continuation_token: String? =
            row.value.musicPlaylistShelfRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation

        builder.continuation = continuation_token?.let {
            RadioContinuation(it, RadioContinuation.Type.PLAYLIST)
        }
        builder.item_set_ids = if (row_items.all { it.second != null }) row_items.map { it.second!! } else null

        // Playlists don't display indices
        if (row.value.musicShelfRenderer?.contents?.firstOrNull()?.musicResponsiveListItemRenderer?.index != null) {
            builder.type = YtmPlaylist.Type.ALBUM
        }

        if (builder.description != null) {
            break
        }
    }

    return@runCatching builder.build()
}
