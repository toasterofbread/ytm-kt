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
import dev.toastbits.ytmkt.model.external.ItemLayoutType
import dev.toastbits.ytmkt.model.internal.MusicCardShelfRenderer
import dev.toastbits.ytmkt.model.internal.NavigationEndpoint
import dev.toastbits.ytmkt.model.internal.TextRuns
import dev.toastbits.ytmkt.model.internal.YoutubeiShelf
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put

open class YTMSearchEndpoint(override val api: YoutubeiApi): SearchEndpoint() {
    override suspend fun searchMusic(
        query: String,
        params: String?
    ): Result<SearchResults> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("search")
            addApiHeadersWithAuthenticated()
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
                        type = ItemLayoutType.CARD
                    ),
                    null
                ))
                continue
            }

            val shelf: YTMGetSongFeedEndpoint.MusicShelfRenderer = category.value.musicShelfRenderer ?: continue
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
                                is YtmSong ->
                                    if (item.type == YtmSong.Type.VIDEO) SearchType.VIDEO else SearchType.SONG

                                is YtmArtist ->
                                    SearchType.ARTIST

                                is YtmPlaylist ->
                                    when (item.type) {
                                        YtmPlaylist.Type.ALBUM -> SearchType.ALBUM
                                        else -> SearchType.PLAYLIST
                                    }

                                else -> throw NotImplementedError(item::class.toString())
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
    data class Contents(val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer)
    @Serializable
    data class TabbedSearchResultsRenderer(val tabs: List<Tab>)
    @Serializable
    data class Tab(val tabRenderer: TabRenderer)
    @Serializable
    data class TabRenderer(val content: Content?)
    @Serializable
    data class Content(val sectionListRenderer: SectionListRenderer)
    @Serializable
    data class SectionListRenderer(
        val contents: List<YoutubeiShelf>?,
        val header: ChipCloudRendererHeader?
    )
}

@Serializable
data class ChipCloudRenderer(val chips: List<Chip>)
@Serializable
data class Chip(val chipCloudChipRenderer: ChipCloudChipRenderer)
@Serializable
data class ChipCloudChipRenderer(val navigationEndpoint: NavigationEndpoint, val text: TextRuns?)

@Serializable
data class ChipCloudRendererHeader(val chipCloudRenderer: ChipCloudRenderer?)

