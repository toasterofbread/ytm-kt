package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.YoutubeApi

abstract class LoadSongEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun loadSong(song_id: String): Result<Song>
}

abstract class LoadArtistEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun loadArtist(artist_id: String): Result<Artist>
}

abstract class LoadPlaylistEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun loadPlaylist(
        playlist_id: String, 
        continuation: MediaItemLayout.Continuation? = null,
        browse_params: String? = null,
        playlist_url: String? = null
    ): Result<Playlist>
}
