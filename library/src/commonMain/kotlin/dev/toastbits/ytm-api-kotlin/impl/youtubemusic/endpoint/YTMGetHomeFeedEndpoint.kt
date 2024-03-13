package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import com.toasterofbread.spmp.model.FilterChip
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.enums.MediaItemType
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.PlainViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.ViewMore
import com.toasterofbread.spmp.model.sortFilterChips
import com.toasterofbread.spmp.resources.uilocalisation.AppLocalisedString
import com.toasterofbread.spmp.resources.uilocalisation.LocalisedString
import com.toasterofbread.spmp.resources.uilocalisation.RawLocalisedString
import com.toasterofbread.spmp.resources.uilocalisation.YoutubeLocalisedString
import dev.toastbits.ytmapi.endpoint.HomeFeedEndpoint
import dev.toastbits.ytmapi.endpoint.HomeFeedLoadResult
import dev.toastbits.ytmapi.impl.youtubemusic.DataParseException
import dev.toastbits.ytmapi.impl.youtubemusic.PLAIN_HEADERS
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.BrowseEndpoint
import dev.toastbits.ytmapi.model.NavigationEndpoint
import dev.toastbits.ytmapi.model.TextRuns
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.model.YoutubeiHeaderContainer
import dev.toastbits.ytmapi.model.YoutubeiShelf
import dev.toastbits.ytmapi.model.YoutubeiShelfContentsItem
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

class YTMGetHomeFeedEndpoint(override val api: YoutubeMusicApi): HomeFeedEndpoint() {
    override suspend fun getHomeFeed(
        min_rows: Int,
        allow_cached: Boolean,
        params: String?,
        continuation: String?
    ): Result<HomeFeedLoadResult> {
        val hl: String = api.data_language

        suspend fun performRequest(ctoken: String?): Result<YoutubeiBrowseResponse> = runCatching {
            val response: HttpResponse = api.client.request {
                endpointPath("browse")

                if (ctoken != null) {
                    url.parameters.append("ctoken", ctoken)
                    url.parameters.append("continuation", ctoken)
                    url.parameters.append("type", "next")
                }

                addAuthApiHeaders()
                addApiHeadersNoAuth(PLAIN_HEADERS)
                postWithBody(
                    params?.let {
                        mapOf("params" to it)
                    }
                )
            }

            return@runCatching response.body
        }

        var data: YoutubeiBrowseResponse = performRequest(continuation).getOrThrow()
        val header_chips: List<FilterChip>? = data.getHeaderChips(api.context)?.sortFilterChips()

        val rows: MutableList<MediaItemLayout> = processRows(data.getShelves(continuation != null), hl).toMutableList()

        var ctoken: String? = data.ctoken
        while (ctoken != null && min_rows >= 1 && rows.size < min_rows) {
            data = performRequest(ctoken).getOrThrow()
            ctoken = data.ctoken

            val shelves = data.getShelves(true)
            if (shelves.isEmpty()) {
                break
            }

            rows.addAll(processRows(shelves, hl))
        }

        return Result.success(HomeFeedLoadResult(rows, ctoken, header_chips))
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
                title: LocalisedString,
                subtitle: LocalisedString? = null,
                view_more: ViewMore? = null,
                type: MediaItemLayout.Type? = null
            ) {
                val items: MutableList<MediaItemData> = row.getMediaItems(hl).toMutableList()
                api.database.transaction {
                    for (item in items) {
                        item.saveToDatabase(api.database)
                    }
                }

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
                    YoutubeLocalisedString.Type.HOME_FEED.createFromKey(header.title!!.first_text, api.context),
                    header.subtitle?.first_text?.let { YoutubeLocalisedString.Type.HOME_FEED.createFromKey(it, api.context) }
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
                    AppLocalisedString(view_more_page_title_key),
                    null,
                    view_more = PlainViewMore(browse_endpoint.browseId!!),
                    type = when(browse_endpoint.browseId) {
                        "FEmusic_listen_again" -> MediaItemLayout.Type.GRID_ALT
                        else -> null
                    }
                )
                continue
            }

            val page_type: String? = browse_endpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
            if (page_type != null && browse_endpoint.browseId != null) {
                val media_item = MediaItemType.fromBrowseEndpointType(page_type).referenceFromId(browse_endpoint.browseId).apply {
                    Title.set(header.title.runs?.getOrNull(0)?.text, api.database)
                }

                add(
                    RawLocalisedString(header.title.first_text),
                    header.subtitle?.first_text?.let { YoutubeLocalisedString.Type.HOME_FEED.createFromKey(it, api.context) },
                    view_more = MediaItemViewMore(media_item, null)
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
