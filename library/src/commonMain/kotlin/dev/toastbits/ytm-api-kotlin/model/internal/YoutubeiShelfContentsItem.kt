package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.YoutubeApi

data class YoutubeiShelfContentsItem(
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
    val musicMultiRowListItemRenderer: MusicMultiRowListItemRenderer?
) {
    // Pair(item, playlistSetVideoId)
    fun toMediaItemData(hl: String, api: YoutubeApi): Pair<MediaItem, String?>? {
        if (musicTwoRowItemRenderer != null) {
            return musicTwoRowItemRenderer.toMediaItem(api)?.let { Pair(it, null) }
        }
        else if (musicResponsiveListItemRenderer != null) {
            return musicResponsiveListItemRenderer.toMediaItemAndPlaylistSetVideoId(hl)
        }
        else if (musicMultiRowListItemRenderer != null) {
            return Pair(musicMultiRowListItemRenderer.toMediaItem(hl), null)
        }

        throw NotImplementedError()
    }
}
