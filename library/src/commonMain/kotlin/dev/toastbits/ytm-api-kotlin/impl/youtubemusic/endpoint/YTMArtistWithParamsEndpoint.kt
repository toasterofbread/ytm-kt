package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.YoutubePage
import dev.toastbits.ytmapi.endpoint.ArtistWithParamsEndpoint
import dev.toastbits.ytmapi.endpoint.ArtistWithParamsRow
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMArtistWithParamsEndpoint(override val api: YoutubeMusicApi): ArtistWithParamsEndpoint() {
    override suspend fun loadArtistWithParams(
        browse_params: YoutubePage.BrowseParamsData
    ): Result<List<ArtistWithParamsRow>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", browse_params.browse_id)
                put("params", browse_params.browse_params)
            }
        }

        val data: YoutubeiBrowseResponse = response.body()

        val row_content = data.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: emptyList()

        return@runCatching row_content.map { row ->
            val items: List<MediaItem> = row.getMediaItemsOrNull(hl, api).orEmpty()
            ArtistWithParamsRow(
                title = row.title?.text,
                items = items
            )
        }
    }
}
