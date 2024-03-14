package dev.toastbits.ytmapi.itemcache

import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.radio.RadioContinuation
import dev.toastbits.ytmapi.YoutubeApi

open class MediaItemCache {
    open fun getSong(song_id: String, keys: Set<SongKey>): Song? = null
    open fun getPlaylist(playlist_id: String, keys: Set<PlaylistKey>): Playlist? = null
    open fun getArtist(artist_id: String, keys: Set<ArtistKey>): Artist? = null

    enum class SongKey {
        ARTIST_ID,
        RELATED_BROWSE_ID,
        TYPE
    }

    enum class PlaylistKey {
        ARTIST_ID,
        ITEMS,
        CONTINUATION
    }

    enum class ArtistKey {
        LAYOUTS
    }

    suspend fun loadSong(api: YoutubeApi, song_id: String, keys: Set<SongKey>): Song {
        val song: Song? = getSong(song_id, keys)
        if (song != null) {
            return song
        }
        return api.LoadSong.loadSong(song_id).getOrThrow()
    }

    suspend fun loadPlaylist(api: YoutubeApi, playlist_id: String, keys: Set<PlaylistKey>): Playlist {
        val playlist: Playlist? = getPlaylist(playlist_id, keys)
        if (playlist != null) {
            return playlist
        }
        return api.LoadPlaylist.loadPlaylist(playlist_id).getOrThrow()
    }

    suspend fun loadArtist(api: YoutubeApi, artist_id: String, keys: Set<ArtistKey>): Artist {
        val artist: Artist? = getArtist(artist_id, keys)
        if (artist != null) {
            return artist
        }
        return api.LoadArtist.loadArtist(artist_id).getOrThrow()
    }
}
