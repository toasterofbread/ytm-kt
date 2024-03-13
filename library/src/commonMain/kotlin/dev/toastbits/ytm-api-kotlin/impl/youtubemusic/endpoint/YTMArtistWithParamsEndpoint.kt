package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.layout.BrowseParamsData
import dev.toastbits.ytmapi.endpoint.ArtistWithParamsEndpoint
import dev.toastbits.ytmapi.endpoint.ArtistWithParamsRow
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse

class YTMArtistWithParamsEndpoint(override val api: YoutubeMusicApi): ArtistWithParamsEndpoint() {
    override suspend fun loadArtistWithParams(
        browse_params: BrowseParamsData
    ): Result<List<ArtistWithParamsRow>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(
                mapOf(
                    "browseId" to browse_params.browse_id,
                    "params" to browse_params.browse_params
                )
            )
        }
            
        val data: YoutubeiBrowseResponse = response.body

        val row_content = data.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: emptyList()

        return@runCatching row_content.map { row ->
            val items: List<MediaItemData> = row.getMediaItemsOrNull(hl).orEmpty()
            ArtistWithParamsRow(
                title = row.title?.text,
                items = items
            )
        }
    }
}
