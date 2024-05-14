package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.uistrings.YoutubeUiString
import dev.toastbits.ytmkt.endpoint.SearchEndpoint
import dev.toastbits.ytmkt.endpoint.SearchFilter
import dev.toastbits.ytmkt.endpoint.SearchResults
import dev.toastbits.ytmkt.endpoint.SearchType
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.model.external.ItemLayoutType
import dev.toastbits.ytmkt.model.internal.MusicCardShelfRenderer
import dev.toastbits.ytmkt.model.internal.NavigationEndpoint
import dev.toastbits.ytmkt.model.internal.TextRuns
import dev.toastbits.ytmkt.model.internal.YoutubeiShelf
import dev.toastbits.ytmkt.model.internal.ItemSectionRenderer
import dev.toastbits.ytmkt.model.internal.DidYouMeanRenderer
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put

open class YTMSearchEndpoint(override val api: YoutubeiApi): SearchEndpoint() {
    override suspend fun search(
        query: String,
        params: String?,
        non_music: Boolean
    ): Result<SearchResults> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("search", non_music_api = non_music)
            addApiHeadersWithAuthenticated(non_music_api = non_music)
            postWithBody(
                (if (non_music) YoutubeiPostBody.WEB else YoutubeiPostBody.DEFAULT).getPostBody(api)
            ) {
                put("query", query)
                put("params", params)
            }
        }

        val parsed: YoutubeiSearchResponse = response.body()

        val section_list_renderers: List<SectionListRenderer> = parsed.contents.getSectionListRenderers() ?: emptyList()

        var correction_suggestion: String? = null

        val categories: List<YoutubeiShelf> =
            section_list_renderers.flatMap { renderer ->
                renderer.contents.orEmpty().filter { shelf ->
                    val did_you_mean_renderer: DidYouMeanRenderer? = shelf.itemSectionRenderer?.contents?.firstOrNull()?.didYouMeanRenderer

                    if (did_you_mean_renderer != null) {
                        did_you_mean_renderer.correctedQuery?.first_text?.also {
                            correction_suggestion = it
                        }
                        return@filter false
                    }
                    else {
                        return@filter true
                    }
                }
            }

        val category_layouts: MutableList<Pair<MediaItemLayout, SearchFilter?>> = mutableListOf()
        val chips = section_list_renderers.flatMap { it.header?.chipCloudRenderer?.chips ?: emptyList() }

        for ((index, category) in categories.withIndex()) {
            val card: MusicCardShelfRenderer? = category.musicCardShelfRenderer
            val key: String? = card?.header?.musicCardShelfHeaderBasicRenderer?.title?.firstTextOrNull()
            if (key != null) {
                category_layouts.add(Pair(
                    MediaItemLayout(
                        mutableListOf(card.getMediaItem()),
                        YoutubeUiString.Type.SEARCH_PAGE.createFromKey(key, hl),
                        null,
                        type = ItemLayoutType.CARD
                    ),
                    null
                ))
                continue
            }

            val item_section_renderer: ItemSectionRenderer? = category.itemSectionRenderer
            if (item_section_renderer != null) {
                category_layouts.add(
                    Pair(MediaItemLayout(item_section_renderer.getMediaItems(), null, null), null)
                )
                continue
            }

            val shelf: YTMGetSongFeedEndpoint.MusicShelfRenderer = category.musicShelfRenderer ?: continue
            val items = shelf.contents?.mapNotNull { it.toMediaItemData(hl, api)?.first }?.toMutableList() ?: continue
            val search_params = if (index == 0) null else chips?.get(index - 1)?.chipCloudChipRenderer?.navigationEndpoint?.searchEndpoint?.params

            val title: String? = shelf.title?.firstTextOrNull()
            if (title != null) {
                category_layouts.add(Pair(
                    MediaItemLayout(items, YoutubeUiString.Type.SEARCH_PAGE.createFromKey(title, hl), null),
                    search_params?.let {
                        val item = items.firstOrNull() ?: return@let null
                        SearchFilter(
                            when (item) {
                                is YtmSong ->
                                    if (item.type == YtmSong.Type.VIDEO) SearchType.VIDEO else SearchType.SONG

                                is YtmArtist ->
                                    SearchType.ARTIST

                                is YtmPlaylist ->
                                    when (item.type) {
                                        YtmPlaylist.Type.ALBUM -> SearchType.ALBUM
                                        else -> SearchType.PLAYLIST
                                    }
                            },
                            it
                        )
                    }
                ))
            }
        }

        if (correction_suggestion == null && query.trim().lowercase() == "recursion") {
            correction_suggestion = query
        }

        return@runCatching SearchResults(category_layouts, correction_suggestion)
    }
}

@Serializable
private data class YoutubeiSearchResponse(
    val contents: Contents
) {
    @Serializable
    data class Contents(val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer?, val twoColumnSearchResultsRenderer: TwoColumnSearchResultsRenderer?) {
        fun getSectionListRenderers(): List<SectionListRenderer>? =
            tabbedSearchResultsRenderer?.tabs?.mapNotNull { it.tabRenderer.content?.sectionListRenderer }
            ?: twoColumnSearchResultsRenderer?.primaryContents?.let { listOf(it.sectionListRenderer) }
    }

    @Serializable
    data class TabbedSearchResultsRenderer(val tabs: List<Tab>) {
        @Serializable
        data class Tab(val tabRenderer: TabRenderer)
        @Serializable
        data class TabRenderer(val content: Content?)
    }

    @Serializable
    data class TwoColumnSearchResultsRenderer(val primaryContents: Content)

    @Serializable
    data class Content(val sectionListRenderer: SectionListRenderer)
}

@Serializable
data class SectionListRenderer(
    val contents: List<YoutubeiShelf>?,
    val header: ChipCloudRendererHeader?
)

@Serializable
data class ChipCloudRendererHeader(val chipCloudRenderer: ChipCloudRenderer?)
@Serializable
data class ChipCloudRenderer(val chips: List<Chip>)
@Serializable
data class Chip(val chipCloudChipRenderer: ChipCloudChipRenderer)
@Serializable
data class ChipCloudChipRenderer(val navigationEndpoint: NavigationEndpoint, val text: TextRuns?)
