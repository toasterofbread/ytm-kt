package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.loader.MediaItemLoader
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.SongRelatedContentEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.RelatedGroup
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse

class YTMSongRelatedContentEndpoint(override val api: YoutubeMusicApi): SongRelatedContentEndpoint() {
    override suspend fun getSongRelated(
        song_id: String
    ): Result<List<RelatedGroup>> = runCatching {
        var related_browse_id: String? = api.item_cache.getSongRelatedBrowseId(song_id)

        if (related_browse_id == null) {
            val load_result: Result<Song> = api.LoadSong.implementedOrNull()?.loadSong(song_id)

            related_browse_id = load_result.fold(
                { it.related_browse_id },
                { return@runCatching Result.failure(it) }
            )
        }

        if (related_browse_id == null) {
            return@runCatching Result.failure(RuntimeException("Song $song_id has no related_browse_id"))
        }

        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(mapOf("browseId" to related_browse_id))
        }

        val parsed: BrowseResponse = response.body

        val groups = parsed.contents.sectionListRenderer?.contents?.map { group ->
            val items = group.getMediaItemsOrNull(hl)
            for (item in items ?: emptyList()) {
                item.saveToDatabase(api.database)
            }

            RelatedGroup(
                title = group.title?.text,
                items = items,
                description = group.description
            )
        }

        return@runCatching groups!!
    }

    private class BrowseResponse(val contents: YoutubeiBrowseResponse.Content)
}
