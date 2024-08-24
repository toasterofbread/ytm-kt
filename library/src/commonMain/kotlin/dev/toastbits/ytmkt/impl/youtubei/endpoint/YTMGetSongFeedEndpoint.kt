package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.endpoint.SongFeedFilterChip
import dev.toastbits.ytmkt.model.external.*
import dev.toastbits.ytmkt.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.copyWithName
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.uistrings.UiString
import dev.toastbits.ytmkt.uistrings.YoutubeUiString
import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation
import dev.toastbits.ytmkt.endpoint.SongFeedEndpoint
import dev.toastbits.ytmkt.endpoint.SongFeedLoadResult
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.BrowseEndpoint
import dev.toastbits.ytmkt.model.internal.NavigationEndpoint
import dev.toastbits.ytmkt.model.internal.TextRuns
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.model.internal.YoutubeiHeaderContainer
import dev.toastbits.ytmkt.model.internal.YoutubeiShelf
import dev.toastbits.ytmkt.model.internal.YoutubeiShelfContentsItem
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put

private val PLAIN_HEADERS: List<String> = listOf("accept-language", "user-agent", "accept-encoding", "content-encoding", "origin")

open class YTMGetSongFeedEndpoint(override val api: YoutubeiApi): SongFeedEndpoint() {

    /**
     * Returns the title to use for the feed row with the passed browse ID starting with FEmusic_ (such as FEmusic_listen_again).
     *
     * @param browse_id The browse ID of the feed row.
     * @return the desired title of the row, or null to use the textual title.
     */
    open fun getMusicBrowseIdRowTitle(browse_id: String): UiString? = null

    /**
     * Returns the layout type to use for the feed row with the passed browse ID starting with FEmusic_ (such as FEmusic_listen_again).
     *
     * @param browse_id The browse ID of the feed row.
     * @return the desired layout type of the row.
     */
    open fun getMusicBrowseIdRowType(browse_id: String): ItemLayoutType? = null

    override suspend fun getSongFeed(
        min_rows: Int,
        params: String?,
        continuation: String?
    ): Result<SongFeedLoadResult> = runCatching {
        val hl: String = api.data_language

        suspend fun performRequest(ctoken: String?): YoutubeiBrowseResponse {
            val response: HttpResponse = api.client.request {
                endpointPath("browse")

                if (ctoken != null) {
                    url.parameters.append("ctoken", ctoken)
                    url.parameters.append("continuation", ctoken)
                    url.parameters.append("type", "next")
                }

                addApiHeadersWithAuthenticated()
                addApiHeadersWithoutAuthentication(PLAIN_HEADERS)
                postWithBody {
                    if (params != null) {
                        put("params", params)
                    }
                }
            }

            return response.body()
        }

        var data: YoutubeiBrowseResponse = performRequest(continuation)
        val header_chips: List<SongFeedFilterChip>? = data.getHeaderChips(hl)

        val rows: MutableList<MediaItemLayout> = processRows(data.getShelves(continuation != null), hl).toMutableList()

        var ctoken: String? = data.ctoken
        while (ctoken != null && min_rows >= 1 && rows.size < min_rows) {
            data = performRequest(ctoken)
            ctoken = data.ctoken

            val shelves = data.getShelves(true)
            if (shelves.isEmpty()) {
                break
            }

            rows.addAll(processRows(shelves, hl))
        }

        return@runCatching SongFeedLoadResult(rows, ctoken, header_chips)
    }

    private suspend fun processRows(
        rows: List<YoutubeiShelf>,
        hl: String
    ): List<MediaItemLayout> {
        val ret: MutableList<MediaItemLayout> = mutableListOf()
        for (row in rows) {
            val renderer = row.getRenderer()
            if (renderer !is YoutubeiHeaderContainer) {
                continue
            }

            val header = renderer.header?.header_renderer ?: continue

            fun add(
                title: UiString,
                subtitle: UiString? = null,
                view_more: YoutubePage? = null,
                type: ItemLayoutType? = null,
                items: List<YtmMediaItem> = row.getMediaItems(hl, api)
            ) {
                ret.add(
                    MediaItemLayout(
                        items,
                        title, subtitle,
                        view_more = view_more,
                        type = type
                    )
                )
            }

            val browse_endpoint: BrowseEndpoint? = header.title?.runs?.first()?.navigationEndpoint?.browseEndpoint
            if (browse_endpoint == null) {
                val title: UiString = YoutubeUiString.Type.HOME_FEED.createFromKey(header.title!!.first_text, api.data_language)
                val items: List<YtmMediaItem>

                if (title is YoutubeUiString && title.getYoutubeStringId() == YoutubeUILocalisation.StringID.FEED_ROW_RADIOS) {
                    items = row.getMediaItems(hl, api).map { item ->
                        if (item is YtmPlaylist) {
                            item.copy(type = YtmPlaylist.Type.RADIO)
                        }
                        else item
                    }
                }
                else {
                    items = row.getMediaItems(hl, api)
                }

                add(
                    title = title,
                    subtitle = header.subtitle?.first_text?.let { YoutubeUiString.Type.HOME_FEED.createFromKey(it, api.data_language) },
                    items = items
                )
                continue
            }

            if (browse_endpoint.browseId?.startsWith("FEmusic_") == true) {
                val title: UiString =
                    getMusicBrowseIdRowTitle(browse_endpoint.browseId) ?: YoutubeUiString.Type.HOME_FEED.createFromKey(header.title.first_text, api.data_language)

                add(
                    title,
                    null,
                    view_more = PlainYoutubePage(browse_endpoint.browseId),
                    type = getMusicBrowseIdRowType(browse_endpoint.browseId)
                )
                continue
            }

            val page_type: String? = browse_endpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
            val media_item: YtmMediaItem? =
                if (page_type != null && browse_endpoint.browseId != null)
                    YtmMediaItem.Type.fromBrowseEndpointType(page_type)?.itemFromId(browse_endpoint.browseId)
                        ?.copyWithName(name = header.title.runs?.getOrNull(0)?.text)
                else null

            add(
                title = YoutubeUiString.Type.HOME_FEED.createFromKey(header.title.first_text, api.data_language),
                subtitle = header.subtitle?.first_text?.let { YoutubeUiString.Type.HOME_FEED.createFromKey(it, api.data_language) },
                view_more = media_item?.let { MediaItemYoutubePage(it, null) }
            )
        }

        return ret
    }

    @Serializable
    data class MusicShelfRenderer(
        val title: TextRuns?,
        val contents: List<YoutubeiShelfContentsItem>? = null,
        val continuations: List<YoutubeiNextResponse.Continuation>? = null,
        val bottomEndpoint: NavigationEndpoint?
    )
}
