package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.*
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem

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
    fun getMediaItemType(): MediaItem.Type? =
        getPageType()?.let { MediaItem.Type.fromBrowseEndpointType(it) }

    fun getMediaItem(): MediaItem? =
        getPageType()?.let { page_type ->
            browseId?.let { browse_id ->
                MediaItem.Type.fromBrowseEndpointType(page_type).itemFromId(browse_id)
            }
        }

    fun getViewMore(base_item: MediaItem): YoutubePage? {
        val item = getMediaItem()
        if (item != null) {
            return MediaItemYoutubePage(item, params, base_item)
        }
        else if (browseId == null) {
            return null
        }
        else if (params != null) {
            return ListPageBrowseIdYoutubePage(
                base_item,
                list_page_browse_id = browseId,
                browse_params = params
            )
        }
        else {
            return PlainYoutubePage(browseId)
        }
    }
}
data class SearchEndpoint(val query: String, val params: String?)
data class WatchPlaylistEndpoint(val playlistId: String, val params: String)
