package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.enums.PlaylistType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.SongType
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import com.toasterofbread.spmp.resources.uilocalisation.YoutubeLocalisedString
import dev.toastbits.ytmapi.endpoint.SearchEndpoint
import dev.toastbits.ytmapi.endpoint.SearchFilter
import dev.toastbits.ytmapi.endpoint.SearchResults
import dev.toastbits.ytmapi.endpoint.SearchType
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.MusicCardShelfRenderer
import dev.toastbits.ytmapi.model.NavigationEndpoint
import dev.toastbits.ytmapi.model.TextRuns
import dev.toastbits.ytmapi.model.YoutubeiShelf

class YTMSearchEndpoint(override val api: YoutubeMusicApi): SearchEndpoint() {
    override suspend fun searchMusic(
        query: String, 
        params: String?
    ): Result<SearchResults> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("search")
            addAuthApiHeaders()
            postWithBody(mapOf("query" to query, "params" to params))
            
        val parsed: YoutubeiSearchResponse = response.body

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
                        YoutubeLocalisedString.Type.SEARCH_PAGE.createFromKey(key, api.context),
                        null,
                        type = MediaItemLayout.Type.CARD
                    ),
                    null
                ))
                continue
            }

            val shelf: YTMGetHomeFeedEndpoint.MusicShelfRenderer = category.value.musicShelfRenderer ?: continue
            val items = shelf.contents?.mapNotNull { it.toMediaItemData(hl)?.first }?.toMutableList() ?: continue
            val search_params = if (category.index == 0) null else chips?.get(category.index - 1)?.chipCloudChipRenderer?.navigationEndpoint?.searchEndpoint?.params

            val title: String? = shelf.title?.firstTextOrNull()
            if (title != null) {
                category_layouts.add(Pair(
                    MediaItemLayout(items, YoutubeLocalisedString.Type.SEARCH_PAGE.createFromKey(title, api.context), null),
                    search_params?.let {
                        val item = items.firstOrNull() ?: return@let null
                        SearchFilter(when (item) {
                            is SongData -> if (item.song_type == SongType.VIDEO) SearchType.VIDEO else SearchType.SONG
                            is ArtistData -> SearchType.ARTIST
                            is Playlist -> when (item.playlist_type) {
                                PlaylistType.ALBUM -> SearchType.ALBUM
                                else -> SearchType.PLAYLIST
                            }
                            else -> throw NotImplementedError(item.getType().toString())
                        }, it)
                    }
                ))
            }
        }

        api.database.transaction {
            for (category in category_layouts) {
                for (item in category.first.items) {
                    (item as MediaItemData).saveToDatabase(api.database)
                }
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
