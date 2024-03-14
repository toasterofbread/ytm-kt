package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.endpoint.PlaylistContinuationEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmapi.radio.RadioContinuation
import dev.toastbits.ytmapi.itemcache.MediaItemCache
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse

class YTMPlaylistContinuationEndpoint(override val api: YoutubeMusicApi): PlaylistContinuationEndpoint() {
    override suspend fun getPlaylistContinuation(
        initial: Boolean,
        token: String,
        skip_initial: Int,
    ): Result<Pair<List<MediaItem>, RadioContinuation?>> = runCatching {
        if (initial) {
            val playlist: Playlist = api.item_cache.loadPlaylist(
                api,
                playlist_id = token,
                keys = setOf(MediaItemCache.PlaylistKey.ITEMS, MediaItemCache.PlaylistKey.CONTINUATION)
            )

            return@runCatching Pair(
                playlist.items!!.drop(skip_initial),
                playlist.continuation!!
            )
        }

        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            url {
                parameters.append("ctoken", token)
                parameters.append("continuation", token)
                parameters.append("type", "next")
            }
            addAuthApiHeaders()
            postWithBody()
        }

        val data: YoutubeiBrowseResponse = response.body()

        val shelf = data.continuationContents?.musicPlaylistShelfContinuation ?: return@runCatching Pair(emptyList(), null)

        val items: List<MediaItem> =
            shelf.contents!!.mapNotNull { item ->
                item.toMediaItemData(hl, api)?.first
            }

        return@runCatching Pair(
            items.drop(skip_initial),
            shelf.continuations?.firstOrNull()?.nextContinuationData?.continuation?.let {
                RadioContinuation(
                    token = it,
                    type = RadioContinuation.Type.PLAYLIST
                )
            }
        )
    }
}
