package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.SongRelatedContentEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.RelatedGroup
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.itemcache.MediaItemCache
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

class YTMSongRelatedContentEndpoint(override val api: YoutubeMusicApi): SongRelatedContentEndpoint() {
    override suspend fun getSongRelated(
        song_id: String
    ): Result<List<RelatedGroup>> = runCatching {
        val song: Song = api.item_cache.loadSong(
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
            addAuthApiHeaders()
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

    private class BrowseResponse(val contents: YoutubeiBrowseResponse.Content)
}
