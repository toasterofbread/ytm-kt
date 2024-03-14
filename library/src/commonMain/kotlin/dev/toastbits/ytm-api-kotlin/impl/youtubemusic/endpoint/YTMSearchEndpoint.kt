package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.uistrings.YoutubeUiString
import dev.toastbits.ytmapi.endpoint.SearchEndpoint
import dev.toastbits.ytmapi.endpoint.SearchFilter
import dev.toastbits.ytmapi.endpoint.SearchResults
import dev.toastbits.ytmapi.endpoint.SearchType
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.internal.MusicCardShelfRenderer
import dev.toastbits.ytmapi.model.internal.NavigationEndpoint
import dev.toastbits.ytmapi.model.internal.TextRuns
import dev.toastbits.ytmapi.model.internal.YoutubeiShelf
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMSearchEndpoint(override val api: YoutubeMusicApi): SearchEndpoint() {
    override suspend fun searchMusic(
        query: String,
        params: String?
    ): Result<SearchResults> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("search")
            addAuthApiHeaders()
            postWithBody {
                put("query", query)
                put("params", params)
            }
        }

        val parsed: YoutubeiSearchResponse = response.body()

        val tab = parsed.contents.tabbedSearchResultsRenderer.tabs.first().tabRenderer

        var correction_suggestion: String? = null
        val categories: List<YoutubeiShelf> = tab.content?.sectionListRenderer?.contents?.filter { shelf ->
            if (shelf.itemSectionRenderer != null) {
                shelf.itemSectionRenderer.contents.firstOrNull()?.didYouMeanRenderer?.correctedQuery?.first_text?.also {
                    correction_suggestion = it
                }
                false
            }
            else {
                true
            }
        } ?: emptyList()

        val category_layouts: MutableList<Pair<MediaItemLayout, SearchFilter?>> = mutableListOf()
        val chips = tab.content?.sectionListRenderer?.header?.chipCloudRenderer?.chips

        for (category in categories.withIndex()) {
            val card: MusicCardShelfRenderer? = category.value.musicCardShelfRenderer
            val key: String? = card?.header?.musicCardShelfHeaderBasicRenderer?.title?.firstTextOrNull()
            if (key != null) {
                category_layouts.add(Pair(
                    MediaItemLayout(
                        mutableListOf(card.getMediaItem()),
                        YoutubeUiString.Type.SEARCH_PAGE.createFromKey(key, hl),
                        null,
                        type = MediaItemLayout.Type.CARD
                    ),
                    null
                ))
                continue
            }

            val shelf: YTMGetHomeFeedEndpoint.MusicShelfRenderer = category.value.musicShelfRenderer ?: continue
            val items = shelf.contents?.mapNotNull { it.toMediaItemData(hl, api)?.first }?.toMutableList() ?: continue
            val search_params = if (category.index == 0) null else chips?.get(category.index - 1)?.chipCloudChipRenderer?.navigationEndpoint?.searchEndpoint?.params

            val title: String? = shelf.title?.firstTextOrNull()
            if (title != null) {
                category_layouts.add(Pair(
                    MediaItemLayout(items, YoutubeUiString.Type.SEARCH_PAGE.createFromKey(title, hl), null),
                    search_params?.let {
                        val item = items.firstOrNull() ?: return@let null
                        SearchFilter(
                            when (item) {
                                is Song ->
                                    if (item.type == Song.Type.VIDEO) SearchType.VIDEO else SearchType.SONG

                                is Artist ->
                                    SearchType.ARTIST

                                is Playlist ->
                                    when (item.type) {
                                        Playlist.Type.ALBUM -> SearchType.ALBUM
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

private data class YoutubeiSearchResponse(
    val contents: Contents
) {
    data class Contents(val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer)
    data class TabbedSearchResultsRenderer(val tabs: List<Tab>)
    data class Tab(val tabRenderer: TabRenderer)
    data class TabRenderer(val content: Content?)
    data class Content(val sectionListRenderer: SectionListRenderer)
    data class SectionListRenderer(
        val contents: List<YoutubeiShelf>?,
        val header: ChipCloudRendererHeader?
    )
}

data class ChipCloudRenderer(val chips: List<Chip>)
data class Chip(val chipCloudChipRenderer: ChipCloudChipRenderer)
data class ChipCloudChipRenderer(val navigationEndpoint: NavigationEndpoint, val text: TextRuns?)

data class ChipCloudRendererHeader(val chipCloudRenderer: ChipCloudRenderer?)

