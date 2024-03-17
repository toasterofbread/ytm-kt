package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.YoutubePage
import dev.toastbits.ytmkt.endpoint.ArtistWithParamsEndpoint
import dev.toastbits.ytmkt.endpoint.ArtistWithParamsRow
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

open class YTMArtistWithParamsEndpoint(override val api: YoutubeiApi): ArtistWithParamsEndpoint() {
    override suspend fun loadArtistWithParams(
        browse_params: YoutubePage.BrowseParamsData
    ): Result<List<ArtistWithParamsRow>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("browseId", browse_params.browse_id)
                put("params", browse_params.browse_params)
            }
        }

        val data: YoutubeiBrowseResponse = response.body()

        val row_content = data.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: emptyList()

        return@runCatching row_content.map { row ->
            val items: List<YtmMediaItem> = row.getMediaItemsOrNull(hl, api).orEmpty()
            ArtistWithParamsRow(
                title = row.title?.text,
                items = items
            )
        }
    }
}
