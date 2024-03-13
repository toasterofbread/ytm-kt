package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.ChipCloudRendererHeader
import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.YTMGetHomeFeedEndpoint
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

data class YoutubeiBrowseResponse(
    val contents: Contents?,
    val continuationContents: ContinuationContents?,
    val header: Header?
) {
    val ctoken: String?
        get() = continuationContents?.sectionListContinuation?.continuations?.firstOrNull()?.nextContinuationData?.continuation
            ?: contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.continuations?.firstOrNull()?.nextContinuationData?.continuation

    fun getShelves(has_continuation: Boolean): List<YoutubeiShelf> {
        return if (has_continuation) continuationContents?.sectionListContinuation?.contents ?: emptyList()
        else contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents ?: emptyList()
    }

    data class Contents(
        val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer?,
        val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer?
    )
    data class SingleColumnBrowseResultsRenderer(val tabs: List<Tab>)
    data class Tab(val tabRenderer: TabRenderer)
    data class TabRenderer(val content: Content?)
    data class Content(val sectionListRenderer: SectionListRenderer?)

    data class SectionListRenderer(val contents: List<YoutubeiShelf>?, val header: ChipCloudRendererHeader?, val continuations: List<YoutubeiNextResponse.Continuation>?)
    class TwoColumnBrowseResultsRenderer(val tabs: List<Tab>, val secondaryContents: SecondaryContents) {
        class SecondaryContents(val sectionListRenderer: SectionListRenderer)
    }

    data class ContinuationContents(val sectionListContinuation: SectionListRenderer?, val musicPlaylistShelfContinuation: YTMGetHomeFeedEndpoint.MusicShelfRenderer?)
}
