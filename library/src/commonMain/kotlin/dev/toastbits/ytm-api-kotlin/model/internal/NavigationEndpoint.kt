package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.ViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData

data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint?,
    val browseEndpoint: BrowseEndpoint?,
    val searchEndpoint: SearchEndpoint?,
    val watchPlaylistEndpoint: WatchPlaylistEndpoint?,
    val channelCreationFormEndpoint: ChannelCreationFormEndpoint?
) {
    fun getMediaItem(): MediaItemData? {
        if (watchEndpoint != null) {
            if (watchEndpoint.videoId != null) {
                return SongData(watchEndpoint.videoId)
            }
            else if (watchEndpoint.playlistId != null) {
                return Playlist(watchEndpoint.playlistId)
            }
        }
        if (browseEndpoint != null) {
            browseEndpoint.getMediaItem()?.also { return it }
        }
        if (watchPlaylistEndpoint != null) {
            return Playlist(watchPlaylistEndpoint.playlistId)
        }
        return null
    }

    fun getViewMore(item: MediaItem): ViewMore? {
        if (browseEndpoint != null) {
            browseEndpoint.getViewMore(item).also { return it }
        }
        return getMediaItem()?.let { MediaItemViewMore(it, null, item) }
    }
}

data class ChannelCreationFormEndpoint(val channelCreationToken: String)
