package dev.toastbits.ytmapi.impl.youtubemusic.loadmediaitem

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.ArtistLayout
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.model.external.MediaItemYoutubePage
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.ArtistBuilder
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.model.external.YoutubePage
import dev.toastbits.ytmapi.model.external.ItemLayoutType
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

suspend fun parseArtistResponse(
    artist_id: String,
    response: HttpResponse,
    hl: String,
    api: YoutubeApi
): Result<Artist> = runCatching {
    val parsed: YoutubeiBrowseResponse = response.body()
    val builder: ArtistBuilder = ArtistBuilder(artist_id)

    val header_renderer: HeaderRenderer? = parsed.header?.getRenderer()
    if (header_renderer != null) {
        builder.name = header_renderer.title!!.first_text
        builder.thumbnail_provider = ThumbnailProvider.fromThumbnails(header_renderer.getThumbnails())

        if (header_renderer.subscriptionButton != null) {
            val subscribe_button = header_renderer.subscriptionButton.subscribeButtonRenderer
            builder.subscribe_channel_id = subscribe_button.channelId
            builder.subscriber_count = parseYoutubeSubscribersString(subscribe_button.subscriberCountText.first_text, hl)
            builder.subscribed = subscribe_button.subscribed
        }
        if (header_renderer.playButton?.buttonRenderer?.icon?.iconType == "MUSIC_SHUFFLE") {
            builder.shuffle_playlist_id = header_renderer.playButton.buttonRenderer.navigationEndpoint.watchEndpoint?.playlistId
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

    var layouts: MutableList<ArtistLayout>? = null

    for (row in section_list_renderer?.contents.orEmpty().withIndex()) {
        if (row.value.description != null) {
            continue
        }

        val items: List<MediaItem> =
            row.value.getMediaItemsAndSetIds(hl, api).map {
                it.first
            }

        val continuation_token: String? =
            row.value.musicPlaylistShelfRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation

        val layout_title = row.value.title?.text?.let {
            if (artist_id == api.user_auth_state?.own_channel_id) YoutubeUiString.Type.OWN_CHANNEL.createFromKey(it, hl)
            else YoutubeUiString.mediaItemPage(it, MediaItem.Type.ARTIST, hl)
        }

        val view_more: YoutubePage? = row.value.getNavigationEndpoint()?.getViewMore(Artist(artist_id))

        val new_layout = ArtistLayout(
            items = items,
            title = layout_title,
            type = if (row.index == 0) ItemLayoutType.NUMBERED_LIST else ItemLayoutType.GRID,
            view_more = view_more,
            playlist_id = continuation_token
        )

        if (layouts == null) {
            layouts = mutableListOf(new_layout)
        }
        else {
            layouts.add(new_layout)
        }
    }

    builder.layouts = layouts

    return@runCatching builder.build()
}
