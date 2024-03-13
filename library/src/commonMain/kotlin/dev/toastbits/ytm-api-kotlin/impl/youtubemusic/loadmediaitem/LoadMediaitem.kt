package dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.artist.ArtistLayout
import dev.toastbits.ytmapi.model.external.mediaitem.enums.MediaItemType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.PlaylistType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.SongType
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylistRef
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import com.toasterofbread.spmp.resources.uilocalisation.YoutubeLocalisedString
import com.toasterofbread.spmp.resources.uilocalisation.parseYoutubeDurationString
import com.toasterofbread.spmp.resources.uilocalisation.parseYoutubeSubscribersString
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.fromJson
import dev.toastbits.ytmapi.getReader
import dev.toastbits.ytmapi.model.Header
import dev.toastbits.ytmapi.model.HeaderRenderer
import dev.toastbits.ytmapi.model.TextRun
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse
import io.ktor.client.statement.HttpResponse

class InvalidRadioException: Throwable()

suspend fun processDefaultResponse(
    item: MediaItemData,
    response: HttpResponse,
    hl: String,
    api: YoutubeApi
): Result<Unit> = runCatching {
    if (item is SongData) {
        return@runCatching processSong(item.id, response, api).getOrThrow()
    }

    val parsed: YoutubeiBrowseResponse = response.body

    // Skip unneeded information for radios
    if (item is Playlist && item.playlist_type == PlaylistType.RADIO) {
        val playlist_shelf = parsed
            .contents!!
            .singleColumnBrowseResultsRenderer!!
            .tabs[0]
            .tabRenderer
            .content!!
            .sectionListRenderer!!
            .contents!![0]
            .musicPlaylistShelfRenderer!!

        item.items = playlist_shelf.contents!!.mapNotNull { data ->
            val data_item = data.toMediaItemData(hl)?.first
            if (data_item is SongData) {
                return@mapNotNull data_item
            }
            return@mapNotNull null
        }

        val continuation = playlist_shelf.continuations?.firstOrNull()?.nextRadioContinuationData?.continuation
        if (continuation != null) {
            item.continuation = MediaItemLayout.Continuation(continuation, MediaItemLayout.Continuation.Type.SONG, item.id)
        }

        val header_renderer = parsed.header?.getRenderer()
        if (header_renderer != null) {
            item.thumbnail_provider = ThumbnailProvider.fromThumbnails(header_renderer.getThumbnails())
        }

        return@runCatching
    }

    val header_renderer: HeaderRenderer? = parsed.header?.getRenderer()
    if (header_renderer != null) {
        item.title = header_renderer.title!!.first_text
        item.description = header_renderer.description?.first_text

        if (item !is Song) {
            item.thumbnail_provider = ThumbnailProvider.fromThumbnails(header_renderer.getThumbnails())
        }

        header_renderer.subtitle?.runs?.also { subtitle ->
            if (item is MediaItem.DataWithArtist) {
                val artist_run: TextRun? = subtitle.firstOrNull {
                    it.navigationEndpoint?.browseEndpoint?.let { endpoint ->
                        endpoint.browseId != null && endpoint.getMediaItemType() == MediaItemType.ARTIST
                    } ?: false
                }

                if (artist_run != null) {
                    item.artist = ArtistData(artist_run.navigationEndpoint!!.browseEndpoint!!.browseId!!).apply {
                        title = artist_run.text
                    }
                }
            }

            if (item is Playlist) {
                item.year = subtitle.lastOrNull { last_run ->
                    last_run.text.all { it.isDigit() }
                }?.text?.toInt()
            }
        }

        if (item is Playlist) {
            header_renderer.secondSubtitle?.runs?.also { second_subtitle ->
                for (run in second_subtitle.reversed().withIndex()) {
                    when (run.index) {
                        0 -> item.total_duration = parseYoutubeDurationString(run.value.text, hl)
                        1 -> item.item_count = run.value.text.filter { it.isDigit() }.toInt()
                    }
                }
            }
        }

        if (item is ArtistData) {
            if (header_renderer.subscriptionButton != null) {
                val subscribe_button = header_renderer.subscriptionButton.subscribeButtonRenderer
                item.subscribe_channel_id = subscribe_button.channelId
                item.subscriber_count = parseYoutubeSubscribersString(subscribe_button.subscriberCountText.first_text, hl)
                item.subscribed = subscribe_button.subscribed
            }
            if (header_renderer.playButton?.buttonRenderer?.icon?.iconType == "MUSIC_SHUFFLE") {
                item.shuffle_playlist_id = header_renderer.playButton.buttonRenderer.navigationEndpoint.watchEndpoint?.playlistId
            }
        }
    }

    if (item is Playlist) {
        val menu_buttons: List<Header.TopLevelButton>? =
            parsed.header?.musicDetailHeaderRenderer?.menu?.menuRenderer?.topLevelButtons

        if (menu_buttons?.any { it.buttonRenderer?.icon?.iconType == "EDIT" } == true) {
            item.owner = api.user_auth_state?.own_channel
            item.playlist_type = PlaylistType.PLAYLIST
        }
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
        val description = row.value.description
        if (description != null) {
            item.description = description
            continue
        }

        val items: List<Pair<MediaItemData, String?>> = row.value.getMediaItemsAndSetIds(hl)
        val items_mapped: List<MediaItemData> = items.map {
            val list_item: MediaItemData = it.first
            if (item is Artist && list_item is SongData && list_item.song_type == SongType.PODCAST) {
                list_item.artist = item
            }
            list_item
        }

        val continuation_token: String? =
            row.value.musicPlaylistShelfRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation

        if (item is Playlist) {
            item.items = items_mapped.filterIsInstance<SongData>()
            item.continuation = continuation_token?.let {
                MediaItemLayout.Continuation(
                    it,
                    MediaItemLayout.Continuation.Type.PLAYLIST
                )
            }
            item.item_set_ids = if (items.all { it.second != null }) items.map { it.second!! } else null

            // Playlists's don't display indices
            if (row.value.musicShelfRenderer?.contents?.firstOrNull()?.musicResponsiveListItemRenderer?.index != null) {
                item.playlist_type = PlaylistType.ALBUM
            }

            break
        }

        check(item is ArtistData)

        val layout_title = row.value.title?.text?.let {
            if (item.isOwnChannel(api)) YoutubeLocalisedString.Type.OWN_CHANNEL.createFromKey(it, api.context)
            else YoutubeLocalisedString.mediaItemPage(it, item.getType(), api.context)
        }

        val view_more = row.value.getNavigationEndpoint()?.getViewMore(item)
        if (view_more is MediaItemViewMore) {
            val view_more_item = view_more.media_item as MediaItemData
            if (view_more_item is MediaItem.DataWithArtist) {
                view_more_item.artist = item
            }
        }

        val new_layout = ArtistLayout.create(item.id).also { layout ->
            layout.items = items_mapped.toMutableList()
            layout.title = layout_title
            layout.type = if (row.index == 0) MediaItemLayout.Type.NUMBERED_LIST else MediaItemLayout.Type.GRID
            layout.view_more = view_more
            layout.playlist = continuation_token?.let {
                RemotePlaylistRef(it)
            }
        }

        if (item.layouts == null) {
            item.layouts = mutableListOf()
        }
        item.layouts!!.add(new_layout)
    }
}
