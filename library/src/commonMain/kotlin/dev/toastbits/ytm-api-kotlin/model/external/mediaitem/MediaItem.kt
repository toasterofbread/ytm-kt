package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ThumbnailProvider

sealed class MediaItem {
    abstract val id: String
    abstract val name: String?
    abstract val thumbnail_provider: ThumbnailProvider?

    enum class Type {
        SONG, ARTIST, PLAYLIST;

        fun itemFromId(id: String): MediaItem = when (this) {
            SONG -> Song(id)
            ARTIST -> Artist(id)
            PLAYLIST -> Playlist(id)
        }

        companion object {
            fun fromBrowseEndpointType(page_type: String): MediaItem.Type {
                // Remove "MUSIC_PAGE_TYPE_" prefix
                val type_name: String = page_type.substring(16)

                if (type_name.startsWith("ARTIST")) {
                    return ARTIST
                }
                if (type_name.startsWith("PODCAST")) {
                    return PLAYLIST
                }

                return when (type_name) {
                    "PLAYLIST",
                    "ALBUM",
                    "AUDIOBOOK",
                    "RADIO" ->
                        PLAYLIST
                    "USER_CHANNEL", "LIBRARY_ARTIST" ->
                        ARTIST
                    "NON_MUSIC_AUDIO_TRACK_PAGE", "UNKNOWN" ->
                        SONG
                    else -> throw NotImplementedError(page_type)
                }
            }
        }
    }
}

fun <T: MediaItem> T.copyWithName(name: String?): T =
    when (val item = this as MediaItem) {
        is Song -> item.copy(name = name) as T
        is Artist -> item.copy(name = name) as T
        is Playlist -> item.copy(name = name) as T
    }
