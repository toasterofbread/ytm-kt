package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.ThumbnailProvider

data class Song(
    override val id: String,
    override val name: String? = null,
    override val thumbnail_provider: ThumbnailProvider? = null,
    val artist: Artist? = null,
    val type: Type? = null,
    val is_explicit: Boolean = false,
    val album: Playlist? = null,
    val duration: Long? = null,
    val related_browse_id: String? = null,
    val lyrics_browse_id: String? = null
): MediaItem() {
    enum class Type {
        SONG, VIDEO, PODCAST
    }
}
