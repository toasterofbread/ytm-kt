package dev.toastbits.ytmapi.radio

import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.radio.RadioContinuation
import dev.toastbits.ytmapi.RadioBuilderModifier
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Song

data class RadioContinuation(
    val token: String,
    val type: Type,
    val song_id: String? = null,
    val playlist_skip_amount: Int = 0
) {
    enum class Type {
        SONG,
        PLAYLIST,
        PLAYLIST_INITIAL
    }

    init {
        if (type == Type.SONG) {
            require(song_id != null)
        }
    }

    suspend fun loadContinuation(
        api: YoutubeApi,
        filters: List<RadioBuilderModifier> = emptyList()
    ): Result<Pair<List<MediaItem>, RadioContinuation?>> =
        when (type) {
            Type.SONG -> loadSongContinuation(filters, api)
            Type.PLAYLIST -> loadPlaylistContinuation(false, api)
            Type.PLAYLIST_INITIAL -> loadPlaylistContinuation(true, api)
        }

    private suspend fun loadSongContinuation(
        filters: List<RadioBuilderModifier>,
        api: YoutubeApi
    ): Result<Pair<List<MediaItem>, RadioContinuation?>> = runCatching {
        val radio = api.SongRadio.getSongRadio(song_id!!, token, filters).getOrThrow()
        return@runCatching Pair(
            radio.items,
            radio.continuation?.let {
                copy(token = it)
            }
        )
    }

    private suspend fun loadPlaylistContinuation(
        initial: Boolean,
        api: YoutubeApi
    ): Result<Pair<List<MediaItem>, RadioContinuation?>> = runCatching {
        return api.PlaylistContinuation.getPlaylistContinuation(initial, token, if (initial) playlist_skip_amount else 0)
    }

    private suspend fun loadArtistContinuation(api: YoutubeApi): Result<Pair<List<MediaItem>, String?>> {
        TODO()
    }
}
