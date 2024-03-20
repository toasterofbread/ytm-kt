package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem

abstract class GenericFeedViewMorePageEndpoint: ApiEndpoint() {

    /**
     * Loads a 'view more'-style page by its browse ID.
     *
     * @param browse_id The browse ID of the page.
     * @return a list of media items on the page.
     */
    abstract suspend fun getGenericFeedViewMorePage(browse_id: String): Result<List<YtmMediaItem>>
}
