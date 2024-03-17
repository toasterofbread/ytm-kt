package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem

abstract class GenericFeedViewMorePageEndpoint: ApiEndpoint() {
    abstract suspend fun getGenericFeedViewMorePage(browse_id: String): Result<List<YtmMediaItem>>
}
