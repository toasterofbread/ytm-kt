package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.endpoint.HomeFeedFilterChip
import dev.toastbits.ytmapi.model.external.*
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.copyWithName
import dev.toastbits.ytmapi.uistrings.AppUiString
import dev.toastbits.ytmapi.uistrings.UiString
import dev.toastbits.ytmapi.uistrings.RawUiString
import dev.toastbits.ytmapi.uistrings.YoutubeUiString
import dev.toastbits.ytmapi.endpoint.HomeFeedEndpoint
import dev.toastbits.ytmapi.endpoint.HomeFeedLoadResult
import dev.toastbits.ytmapi.impl.youtubemusic.DataParseException
import dev.toastbits.ytmapi.impl.youtubemusic.PLAIN_HEADERS
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.internal.BrowseEndpoint
import dev.toastbits.ytmapi.model.internal.NavigationEndpoint
import dev.toastbits.ytmapi.model.internal.TextRuns
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.internal.YoutubeiHeaderContainer
import dev.toastbits.ytmapi.model.internal.YoutubeiShelf
import dev.toastbits.ytmapi.model.internal.YoutubeiShelfContentsItem
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMGetHomeFeedEndpoint(override val api: YoutubeMusicApi): HomeFeedEndpoint() {
    override suspend fun getHomeFeed(
        min_rows: Int,
        allow_cached: Boolean,
        params: String?,
        continuation: String?
    ): Result<HomeFeedLoadResult> = runCatching {
        val hl: String = api.data_language

        suspend fun performRequest(ctoken: String?): YoutubeiBrowseResponse {
            val response: HttpResponse = api.client.request {
                endpointPath("browse")

                if (ctoken != null) {
                    url.parameters.append("ctoken", ctoken)
                    url.parameters.append("continuation", ctoken)
                    url.parameters.append("type", "next")
                }

                addAuthApiHeaders()
                addApiHeadersNoAuth(PLAIN_HEADERS)
                postWithBody {
                    if (params != null) {
                        put("params", params)
                    }
                }
            }

            return response.body()
        }

        var data: YoutubeiBrowseResponse = performRequest(continuation)
        val header_chips: List<HomeFeedFilterChip>? = data.getHeaderChips(hl)

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

        return@runCatching HomeFeedLoadResult(rows, ctoken, header_chips)
    }

    private suspend fun processRows(
        rows: List<YoutubeiShelf>,
        hl: String
    ): List<MediaItemLayout> {
        val ret: MutableList<MediaItemLayout> = mutableListOf<MediaItemLayout>()
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
                type: MediaItemLayout.Type? = null
            ) {
                val items: MutableList<MediaItem> = row.getMediaItems(hl, api).toMutableList()
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
                add(
                    YoutubeUiString.Type.HOME_FEED.createFromKey(header.title!!.first_text, api.data_language),
                    header.subtitle?.first_text?.let { YoutubeUiString.Type.HOME_FEED.createFromKey(it, api.data_language) }
                )
                continue
            }

            val view_more_page_title_key: String? =
                when (browse_endpoint.browseId) {
                    "FEmusic_listen_again" -> "home_feed_listen_again"
                    "FEmusic_mixed_for_you" -> "home_feed_mixed_for_you"
                    "FEmusic_new_releases_albums" -> "home_feed_new_releases"
                    "FEmusic_moods_and_genres" -> "home_feed_moods_and_genres"
                    "FEmusic_charts" -> "home_feed_charts"
                    else -> null
                }

            if (view_more_page_title_key != null) {
                add(
                    AppUiString(view_more_page_title_key),
                    null,
                    view_more = PlainYoutubePage(browse_endpoint.browseId!!),
                    type = when(browse_endpoint.browseId) {
                        "FEmusic_listen_again" -> MediaItemLayout.Type.GRID_ALT
                        else -> null
                    }
                )
                continue
            }

            val page_type: String? = browse_endpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
            if (page_type != null && browse_endpoint.browseId != null) {
                val media_item: MediaItem =
                    MediaItem.Type.fromBrowseEndpointType(page_type).itemFromId(browse_endpoint.browseId)
                        .copyWithName(name = header.title.runs?.getOrNull(0)?.text)

                add(
                    RawUiString(header.title.first_text),
                    header.subtitle?.first_text?.let { YoutubeUiString.Type.HOME_FEED.createFromKey(it, api.data_language) },
                    view_more = MediaItemYoutubePage(media_item, null)
                )
            }
        }

        return ret
    }

    data class MusicShelfRenderer(
        val title: TextRuns?,
        val contents: List<YoutubeiShelfContentsItem>? = null,
        val continuations: List<YoutubeiNextResponse.Continuation>? = null,
        val bottomEndpoint: NavigationEndpoint?
    )
}
