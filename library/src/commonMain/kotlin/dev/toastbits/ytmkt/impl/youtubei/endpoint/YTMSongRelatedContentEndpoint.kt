package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.endpoint.SongRelatedContentEndpoint
import dev.toastbits.ytmkt.model.external.RelatedGroup
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable

open class YTMSongRelatedContentEndpoint(override val api: YoutubeiApi): SongRelatedContentEndpoint() {
    override suspend fun getSongRelated(
        song_id: String
    ): Result<List<RelatedGroup>> = runCatching {
        val song: YtmSong = api.item_cache.loadSong(
            api,
            song_id,
            setOf(MediaItemCache.SongKey.RELATED_BROWSE_ID)
        )

        if (song.related_browse_id == null) {
            throw RuntimeException("Song $song_id has no related_browse_id")
        }

        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("browseId", song.related_browse_id)
            }
        }

        val parsed: BrowseResponse = response.body()

        val groups = parsed.contents.sectionListRenderer?.contents?.map { group ->
            RelatedGroup(
                title = group.title?.text,
                items = group.getMediaItemsOrNull(hl, api),
                description = group.description
            )
        }

        return@runCatching groups!!
    }

    @Serializable
    private class BrowseResponse(val contents: YoutubeiBrowseResponse.Content)
}
