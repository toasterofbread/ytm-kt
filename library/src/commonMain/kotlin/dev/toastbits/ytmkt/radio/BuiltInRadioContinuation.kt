package dev.toastbits.ytmkt.radio

import dev.toastbits.ytmkt.endpoint.RadioBuilderModifier
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import kotlinx.serialization.Serializable

@Serializable
data class BuiltInRadioContinuation(
    val token: String,
    val type: Type,
    val item_id: String? = null,
    val playlist_skip_amount: Int = 0
): RadioContinuation {
    enum class Type {
        SONG,
        PLAYLIST,
        PLAYLIST_INITIAL,
        ARTIST_SHUFFLE
    }

    init {
        if (type == Type.SONG) {
            require(item_id != null)
        }
    }

    override suspend fun loadContinuation(
        api: YtmApi,
        filters: List<RadioBuilderModifier>
    ): Result<Pair<List<YtmMediaItem>, BuiltInRadioContinuation?>> =
        when (type) {
            Type.SONG -> loadSongContinuation(filters, api)
            Type.PLAYLIST -> loadPlaylistContinuation(false, api)
            Type.PLAYLIST_INITIAL -> loadPlaylistContinuation(true, api)
            Type.ARTIST_SHUFFLE -> loadArtistShuffleContinuation(api)
        }

    private suspend fun loadSongContinuation(
        filters: List<RadioBuilderModifier>,
        api: YtmApi
    ): Result<Pair<List<YtmMediaItem>, BuiltInRadioContinuation?>> = runCatching {
        val radio = api.SongRadio.getSongRadio(item_id!!, token, filters).getOrThrow()
        return@runCatching Pair(
            radio.items,
            radio.continuation?.let {
                copy(token = it)
            }
        )
    }

    private suspend fun loadPlaylistContinuation(
        initial: Boolean,
        api: YtmApi
    ): Result<Pair<List<YtmMediaItem>, BuiltInRadioContinuation?>> = runCatching {
        return api.PlaylistContinuation.getPlaylistContinuation(initial, token, if (initial) playlist_skip_amount else 0)
    }

    private suspend fun loadArtistShuffleContinuation(api: YtmApi): Result<Pair<List<YtmMediaItem>, BuiltInRadioContinuation?>> {
        return api.ArtistShuffle.getArtistShuffle(item_id!!, token).mapCatching {
            it.items to it.continuation
        }
    }
}
