package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.RelatedGroup

abstract class SongRelatedContentEndpoint: ApiEndpoint() {
    abstract suspend fun getSongRelated(song_id: String): Result<List<RelatedGroup>>
}