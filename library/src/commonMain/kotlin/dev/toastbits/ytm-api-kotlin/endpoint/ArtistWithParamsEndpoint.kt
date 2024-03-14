package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.YoutubePage
import dev.toastbits.ytmapi.YoutubeApi

data class ArtistWithParamsRow(val title: String?, val items: List<MediaItem>)

abstract class ArtistWithParamsEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun loadArtistWithParams(browse_params: YoutubePage.BrowseParamsData): Result<List<ArtistWithParamsRow>>
}
