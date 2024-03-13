package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.YoutubeApi

abstract class GenericFeedViewMorePageEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getGenericFeedViewMorePage(browse_id: String): Result<List<MediaItem>>
}
