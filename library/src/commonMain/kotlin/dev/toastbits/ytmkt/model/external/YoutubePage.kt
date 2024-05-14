package dev.toastbits.ytmkt.model.external

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import kotlinx.serialization.Serializable

@Serializable
sealed interface YoutubePage {
    data class BrowseParamsData(
        val browse_id: String,
        val browse_params: String?
    )

    fun getBrowseParamsData(): YoutubePage.BrowseParamsData?
}

@Serializable
data class MediaItemYoutubePage(
    val browse_media_item: YtmMediaItem,
    val browse_params: String?,
    val media_item: YtmMediaItem? = null
): YoutubePage {
    override fun getBrowseParamsData(): YoutubePage.BrowseParamsData? =
        browse_params?.let { params ->
            YoutubePage.BrowseParamsData(browse_media_item.id, params)
        }
}

@Serializable
data class ListPageBrowseIdYoutubePage(
    val media_item: YtmMediaItem,
    val list_page_browse_id: String,
    val browse_params: String
): YoutubePage {
    override fun getBrowseParamsData(): YoutubePage.BrowseParamsData =
        YoutubePage.BrowseParamsData(list_page_browse_id, browse_params)
}

@Serializable
data class PlainYoutubePage(
    val browse_id: String
): YoutubePage {
    override fun getBrowseParamsData(): YoutubePage.BrowseParamsData =
        YoutubePage.BrowseParamsData(browse_id, null)
}
