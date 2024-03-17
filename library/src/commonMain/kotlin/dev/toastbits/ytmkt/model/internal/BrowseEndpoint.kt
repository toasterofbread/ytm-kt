package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.*
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import kotlinx.serialization.Serializable

@Serializable
data class WatchEndpoint(val videoId: String?, val playlistId: String?)
@Serializable
data class BrowseEndpointContextMusicConfig(val pageType: String)
@Serializable
data class BrowseEndpointContextSupportedConfigs(val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig)
@Serializable
data class BrowseEndpoint(
    val browseId: String?, // Yes, this CAN be null/missing sometimes (YouTube is strange)
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?,
    val params: String?
) {
    fun getPageType(): String? =
        browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
    fun getMediaItemType(): YtmMediaItem.Type? =
        getPageType()?.let { YtmMediaItem.Type.fromBrowseEndpointType(it) }

    fun getMediaItem(): YtmMediaItem? =
        getPageType()?.let { page_type ->
            browseId?.let { browse_id ->
                YtmMediaItem.Type.fromBrowseEndpointType(page_type).itemFromId(browse_id)
            }
        }

    fun getViewMore(base_item: YtmMediaItem): YoutubePage? {
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
@Serializable
data class SearchEndpoint(val query: String, val params: String?)
@Serializable
data class WatchPlaylistEndpoint(val playlistId: String, val params: String)
