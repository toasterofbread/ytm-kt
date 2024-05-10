package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.impl.youtubei.endpoint.YTMGetSongFeedEndpoint
import dev.toastbits.ytmkt.model.YtmApi
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeiShelf(
    val musicShelfRenderer: YTMGetSongFeedEndpoint.MusicShelfRenderer?,
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?,
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
    val musicPlaylistShelfRenderer: YTMGetSongFeedEndpoint.MusicShelfRenderer?,
    val musicCardShelfRenderer: MusicCardShelfRenderer?,
    val gridRenderer: GridRenderer?,
    val itemSectionRenderer: ItemSectionRenderer?
) {
    val title: TextRun? get() =
        if (musicShelfRenderer != null) musicShelfRenderer.title?.runs?.firstOrNull()
        else if (musicCarouselShelfRenderer != null) musicCarouselShelfRenderer.header.getRenderer()?.title?.runs?.firstOrNull()
        else if (musicDescriptionShelfRenderer != null) musicDescriptionShelfRenderer.header?.runs?.firstOrNull()
        else if (musicCardShelfRenderer != null) musicCardShelfRenderer.title.runs?.firstOrNull()
        else if (gridRenderer != null) gridRenderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()
        else null

    val description: String? get() = musicDescriptionShelfRenderer?.description?.first_text

    fun getNavigationEndpoint(): NavigationEndpoint? =
        musicShelfRenderer?.bottomEndpoint ?: musicCarouselShelfRenderer?.header?.getRenderer()?.moreContentButton?.buttonRenderer?.navigationEndpoint

    fun getMediaItemsOrNull(hl: String, api: YtmApi): List<YtmMediaItem>? =
        (musicShelfRenderer?.contents ?: musicCarouselShelfRenderer?.contents ?: musicPlaylistShelfRenderer?.contents ?: gridRenderer?.items)?.mapNotNull {
            it.toMediaItemData(hl, api)?.first
        } ?: itemSectionRenderer?.getMediaItems()

    fun getMediaItems(hl: String, api: YtmApi): List<YtmMediaItem> =
        getMediaItemsOrNull(hl, api)!!

    fun getMediaItemsAndSetIds(hl: String, api: YtmApi): List<Pair<YtmMediaItem, String?>> =
        (musicShelfRenderer?.contents ?: musicCarouselShelfRenderer?.contents ?: musicPlaylistShelfRenderer?.contents ?: gridRenderer?.items)?.mapNotNull {
            it.toMediaItemData(hl, api)
        } ?: itemSectionRenderer?.getMediaItems()?.map { Pair(it, null) } ?: emptyList()

    fun getRenderer(): Any? =
        musicShelfRenderer ?:
        musicCarouselShelfRenderer ?:
        musicDescriptionShelfRenderer ?:
        musicPlaylistShelfRenderer ?:
        musicCardShelfRenderer ?:
        gridRenderer ?:
        itemSectionRenderer
}
