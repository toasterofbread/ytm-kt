package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.endpoint.PlaylistContinuationEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import dev.toastbits.ytmkt.radio.RadioContinuation
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse

open class YTMPlaylistContinuationEndpoint(override val api: YoutubeiApi): PlaylistContinuationEndpoint() {
    override suspend fun getPlaylistContinuation(
        initial: Boolean,
        token: String,
        skip_initial: Int,
    ): Result<Pair<List<YtmMediaItem>, RadioContinuation?>> = runCatching {
        if (initial) {
            val playlist: YtmPlaylist = api.item_cache.loadPlaylist(
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
            addApiHeadersWithAuthenticated()
            postWithBody()
        }

        val data: YoutubeiBrowseResponse = response.body()

        val shelf = data.continuationContents?.musicPlaylistShelfContinuation ?: return@runCatching Pair(emptyList(), null)

        val items: List<YtmMediaItem> =
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
