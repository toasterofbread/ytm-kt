package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.YtmApi
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeiShelfContentsItem(
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
    val musicMultiRowListItemRenderer: MusicMultiRowListItemRenderer?
) {
    // Pair(item, playlistSetVideoId)
    fun toMediaItemData(hl: String, api: YtmApi): Pair<YtmMediaItem, String?>? {
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
