package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.radio.RadioContinuation

abstract class LoadSongEndpoint: ApiEndpoint() {

    /**
     * Loads as much information about the specified song as possible.
     *
     * @param song_id The ID of the song to load.
     * @return a song object containing all known information.
     */
    abstract suspend fun loadSong(song_id: String): Result<YtmSong>
}

abstract class LoadArtistEndpoint: ApiEndpoint() {

    /**
     * Loads as much information about the specified artist as possible.
     *
     * @param artist_id The ID of the artist to load.
     * @return an artist object containing all known information.
     */
    abstract suspend fun loadArtist(artist_id: String): Result<YtmArtist>
}

abstract class LoadPlaylistEndpoint: ApiEndpoint() {
    
    /**
     * Loads as much information about the specified playlist as possible.
     * May also be used to load the continuation of a playlist
     *
     * @param playlist_id The ID of the playlist to load.
     * @param continuation A continuation of this playlist.
     * @param browse_params The browse params to use when loading the playlist, such as from [MediaItemYoutubePage][dev.toastbits.ytmkt.model.external.MediaItemYoutubePage].
     * @param playlist_url The canonical URL pointing to this playlist. May be loaded by the endpoint if not provided. See [YtmPlaylist.playlist_url].
     * @param use_non_music_api Whether to use the non-music API, if configured
     * @return a playlist object containing all known information.
     */
    abstract suspend fun loadPlaylist(
        playlist_id: String,
        continuation: RadioContinuation? = null,
        browse_params: String? = null,
        playlist_url: String? = null,
        use_non_music_api: Boolean = false
    ): Result<YtmPlaylist>
}
