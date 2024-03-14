package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.radio.RadioContinuation

data class Playlist(
    override val id: String,
    override val name: String? = null,
    override val thumbnail_provider: ThumbnailProvider? = null,
    val type: Type? = null,
    val artist: Artist? = null,
    val year: Int? = null,
    val items: List<Song>? = null,
    val owner_id: String? = null,
    val continuation: RadioContinuation? = null,
    val item_set_ids: List<String>? = null,
    val item_count: Int? = null,
    val total_duration: Long? = null
): MediaItem() {
    enum class Type {
        LOCAL, PLAYLIST, ALBUM, AUDIOBOOK, PODCAST, RADIO;

        companion object {
            fun fromBrowseEndpointType(type: String): Type {
                return when (type) {
                    "MUSIC_PAGE_TYPE_PLAYLIST" -> PLAYLIST
                    "MUSIC_PAGE_TYPE_ALBUM" -> ALBUM
                    "MUSIC_PAGE_TYPE_AUDIOBOOK" -> AUDIOBOOK
                    "MUSIC_PAGE_TYPE_PODCAST" -> PODCAST
                    "MUSIC_PAGE_TYPE_RADIO" -> RADIO
                    else -> PLAYLIST
                }
            }
        }
    }
}
