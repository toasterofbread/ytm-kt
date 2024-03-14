package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.*
import dev.toastbits.ytmapi.model.external.mediaitem.*

data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint?,
    val browseEndpoint: BrowseEndpoint?,
    val searchEndpoint: SearchEndpoint?,
    val watchPlaylistEndpoint: WatchPlaylistEndpoint?,
    val channelCreationFormEndpoint: ChannelCreationFormEndpoint?
) {
    fun getMediaItem(): MediaItem? {
        if (watchEndpoint != null) {
            if (watchEndpoint.videoId != null) {
                return Song(watchEndpoint.videoId)
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

    fun getViewMore(item: MediaItem): YoutubePage? {
        if (browseEndpoint != null) {
            browseEndpoint.getViewMore(item).also { return it }
        }
        return getMediaItem()?.let { MediaItemYoutubePage(it, null, item) }
    }
}

data class ChannelCreationFormEndpoint(val channelCreationToken: String)
