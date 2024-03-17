package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.YoutubePage

data class ArtistWithParamsRow(val title: String?, val items: List<YtmMediaItem>)

abstract class ArtistWithParamsEndpoint: ApiEndpoint() {
    abstract suspend fun loadArtistWithParams(browse_params: YoutubePage.BrowseParamsData): Result<List<ArtistWithParamsRow>>
}
