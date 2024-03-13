package dev.toastbits.ytmapi.itemcache

import dev.toastbits.ytmapi.model.external.mediaitem.Playlist

interface MediaItemCache {
    suspend fun getSongRelatedBrowseId(song_id: String): String?
    
    suspend fun getPlaylistArtist(playlist_id: String): String?
    suspend fun getPlaylistItemsAndContinuation(playlist_id: String)
}
