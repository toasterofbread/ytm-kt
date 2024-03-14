package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.YTMGetHomeFeedEndpoint
import dev.toastbits.ytmapi.YoutubeApi

data class YoutubeiShelf(
    val musicShelfRenderer: YTMGetHomeFeedEndpoint.MusicShelfRenderer?,
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?,
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer?,
    val musicPlaylistShelfRenderer: YTMGetHomeFeedEndpoint.MusicShelfRenderer?,
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

    fun getMediaItems(hl: String, api: YoutubeApi): List<MediaItem> =
        (musicShelfRenderer?.contents ?: musicCarouselShelfRenderer?.contents ?: musicPlaylistShelfRenderer?.contents ?: gridRenderer!!.items).mapNotNull {
            it.toMediaItemData(hl, api)?.first
        }

    fun getMediaItemsOrNull(hl: String, api: YoutubeApi): List<MediaItem>? =
        (musicShelfRenderer?.contents ?: musicCarouselShelfRenderer?.contents ?: musicPlaylistShelfRenderer?.contents ?: gridRenderer?.items)?.mapNotNull {
            it.toMediaItemData(hl, api)?.first
        }

    fun getMediaItemsAndSetIds(hl: String, api: YoutubeApi): List<Pair<MediaItem, String?>> =
        (musicShelfRenderer?.contents ?: musicCarouselShelfRenderer?.contents ?: musicPlaylistShelfRenderer?.contents ?: gridRenderer?.items ?: emptyList()).mapNotNull {
            it.toMediaItemData(hl, api)
        }

    fun getRenderer(): Any? =
        musicShelfRenderer ?:
        musicCarouselShelfRenderer ?:
        musicDescriptionShelfRenderer ?:
        musicPlaylistShelfRenderer ?:
        musicCardShelfRenderer ?:
        gridRenderer ?:
        itemSectionRenderer
}
