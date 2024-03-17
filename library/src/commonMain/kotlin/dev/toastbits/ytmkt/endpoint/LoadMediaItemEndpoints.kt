package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.radio.RadioContinuation

abstract class LoadSongEndpoint: ApiEndpoint() {
    abstract suspend fun loadSong(song_id: String): Result<YtmSong>
}

abstract class LoadArtistEndpoint: ApiEndpoint() {
    abstract suspend fun loadArtist(artist_id: String): Result<YtmArtist>
}

abstract class LoadPlaylistEndpoint: ApiEndpoint() {
    abstract suspend fun loadPlaylist(
        playlist_id: String,
        continuation: RadioContinuation? = null,
        browse_params: String? = null,
        playlist_url: String? = null
    ): Result<YtmPlaylist>
}
