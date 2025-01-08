package dev.toastbits.ytmkt.radio

import dev.toastbits.ytmkt.endpoint.RadioBuilderModifier
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem

interface RadioContinuation {
    suspend fun loadContinuation(
        api: YtmApi,
        filters: List<RadioBuilderModifier> = emptyList()
    ): Result<Pair<List<YtmMediaItem>, RadioContinuation?>>
}
