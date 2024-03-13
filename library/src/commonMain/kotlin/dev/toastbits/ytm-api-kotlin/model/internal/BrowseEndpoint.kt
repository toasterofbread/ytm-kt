package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.enums.MediaItemType
import dev.toastbits.ytmapi.model.external.mediaitem.layout.ListPageBrowseIdViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.PlainViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.ViewMore

data class WatchEndpoint(val videoId: String?, val playlistId: String?)
data class BrowseEndpointContextMusicConfig(val pageType: String)
data class BrowseEndpointContextSupportedConfigs(val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig)
data class BrowseEndpoint(
    val browseId: String?, // Yes, this CAN be null/missing sometimes (YouTube is strange)
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?,
    val params: String?
) {
    fun getPageType(): String? =
        browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
    fun getMediaItemType(): MediaItemType? =
        getPageType()?.let { MediaItemType.fromBrowseEndpointType(it) }

    fun getMediaItem(): MediaItemData? =
        getPageType()?.let { page_type ->
            browseId?.let { browse_id ->
                MediaItemData.fromBrowseEndpointType(page_type, browse_id)
            }
        }

    fun getViewMore(base_item: MediaItem): ViewMore? {
        val item = getMediaItem()
        if (item != null) {
            return MediaItemViewMore(item, params, base_item)
        }
        else if (browseId == null) {
            return null
        }
        else if (params != null) {
            return ListPageBrowseIdViewMore(
                base_item.id,
                list_page_browse_id = browseId,
                browse_params = params
            )
        }
        else {
            return PlainViewMore(browseId)
        }
    }
}
data class SearchEndpoint(val query: String, val params: String?)
data class WatchPlaylistEndpoint(val playlistId: String, val params: String)
