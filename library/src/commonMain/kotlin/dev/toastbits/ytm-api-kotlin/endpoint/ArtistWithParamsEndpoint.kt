package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.layout.BrowseParamsData
import dev.toastbits.ytmapi.YoutubeApi

data class ArtistWithParamsRow(val title: String?, val items: List<MediaItemData>)

abstract class ArtistWithParamsEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun loadArtistWithParams(browse_params: BrowseParamsData): Result<List<ArtistWithParamsRow>>
}
