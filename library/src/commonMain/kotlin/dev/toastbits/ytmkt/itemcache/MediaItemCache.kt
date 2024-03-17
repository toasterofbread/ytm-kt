package dev.toastbits.ytmkt.itemcache

import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.YtmApi

open class MediaItemCache {
    open fun getSong(song_id: String, keys: Set<SongKey>): YtmSong? = null
    open fun getArtist(artist_id: String, keys: Set<ArtistKey>): YtmArtist? = null
    open fun getPlaylist(playlist_id: String, keys: Set<PlaylistKey>): YtmPlaylist? = null

    enum class SongKey {
        ARTIST_ID,
        RELATED_BROWSE_ID,
        TYPE
    }

    enum class ArtistKey {
        LAYOUTS
    }

    enum class PlaylistKey {
        ARTIST_ID,
        ITEMS,
        CONTINUATION
    }

    suspend fun loadSong(api: YtmApi, song_id: String, keys: Set<SongKey>): YtmSong {
        val song: YtmSong? = getSong(song_id, keys)
        if (song != null) {
            return song
        }
        return api.LoadSong.loadSong(song_id).getOrThrow()
    }

    suspend fun loadArtist(api: YtmApi, artist_id: String, keys: Set<ArtistKey>): YtmArtist {
        val artist: YtmArtist? = getArtist(artist_id, keys)
        if (artist != null) {
            return artist
        }
        return api.LoadArtist.loadArtist(artist_id).getOrThrow()
    }

    suspend fun loadPlaylist(api: YtmApi, playlist_id: String, keys: Set<PlaylistKey>): YtmPlaylist {
        val playlist: YtmPlaylist? = getPlaylist(playlist_id, keys)
        if (playlist != null) {
            return playlist
        }
        return api.LoadPlaylist.loadPlaylist(playlist_id).getOrThrow()
    }
}
