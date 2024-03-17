package dev.toastbits.ytmkt.impl.youtubei.loadmediaitem

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtistLayout
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtistBuilder
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.model.external.YoutubePage
import dev.toastbits.ytmkt.model.external.ItemLayoutType
import dev.toastbits.ytmkt.uistrings.YoutubeUiString
import dev.toastbits.ytmkt.uistrings.parseYoutubeSubscribersString
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.internal.HeaderRenderer
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body

suspend fun parseArtistResponse(
    artist_id: String,
    response: HttpResponse,
    hl: String,
    api: YtmApi
): Result<YtmArtist> = runCatching {
    val parsed: YoutubeiBrowseResponse = response.body()
    val builder: YtmArtistBuilder = YtmArtistBuilder(artist_id)

    val header_renderer: HeaderRenderer? = parsed.header?.getRenderer()
    if (header_renderer != null) {
        builder.name = header_renderer.title!!.first_text
        builder.description = header_renderer.description?.first_text
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

    var layouts: MutableList<YtmArtistLayout>? = null

    for (row in section_list_renderer?.contents.orEmpty().withIndex()) {
        val description: String? = row.value.description
        if (description != null) {
            builder.description = description
            continue
        }

        val items: List<YtmMediaItem> =
            row.value.getMediaItemsAndSetIds(hl, api).map {
                it.first
            }

        val continuation_token: String? =
            row.value.musicPlaylistShelfRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation

        val layout_title = row.value.title?.text?.let {
            if (artist_id == api.user_auth_state?.own_channel_id) YoutubeUiString.Type.OWN_CHANNEL.createFromKey(it, hl)
            else YoutubeUiString.mediaItemPage(it, YtmMediaItem.Type.ARTIST, hl)
        }

        val view_more: YoutubePage? = row.value.getNavigationEndpoint()?.getViewMore(YtmArtist(artist_id))

        val new_layout = YtmArtistLayout(
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
