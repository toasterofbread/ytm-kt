package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylistRef
import dev.toastbits.ytmapi.endpoint.PlaylistContinuationEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse

class YTMPlaylistContinuationEndpoint(override val api: YoutubeMusicApi): PlaylistContinuationEndpoint() {
    override suspend fun getPlaylistContinuation(
        initial: Boolean,
        token: String,
        skip_initial: Int,
    ): Result<Pair<List<MediaItemData>, String?>> = runCatching {
        if (initial) {
            val playlist = RemotePlaylistRef(token)
            playlist.loadData(api.context, false).onFailure {
                return@runCatching Result.failure(it)
            }

            val items = playlist.Items.get(api.database) ?: return@runCatching Result.failure(IllegalStateException("Items for loaded $playlist is null"))

            return@runCatching Result.success(Pair(
                items.drop(skip_initial).map { it.getEmptyData() },
                playlist.Continuation.get(api.database)?.token
            ))
        }

        val hl = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse?ctoken=$token&continuation=$token&type=next")
            addAuthApiHeaders()
            postWithBody()

        val result = api.performRequest(request)
        val data: YoutubeiBrowseResponse = result.parseJsonResponse {
            return@runCatching Result.failure(it)
        }

        return@runCatching runCatching {
            val shelf = data.continuationContents?.musicPlaylistShelfContinuation ?: return@runCatching Pair(emptyList(), null)

            val items: List<MediaItemData> =
                shelf.contents!!.mapNotNull { item ->
                    item.toMediaItemData(hl)?.first
                }

            val continuation: String? = shelf.continuations?.firstOrNull()?.nextContinuationData?.continuation

            return@runCatching Pair(items.drop(skip_initial), continuation)
        }
    }
}
