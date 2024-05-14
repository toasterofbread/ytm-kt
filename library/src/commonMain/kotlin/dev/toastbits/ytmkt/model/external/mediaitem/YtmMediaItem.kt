package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import kotlinx.serialization.Serializable

@Serializable
sealed interface YtmMediaItem {
    val id: String
    val name: String?
    val description: String?
    val thumbnail_provider: ThumbnailProvider?

    enum class Type {
        SONG, ARTIST, PLAYLIST;

        fun itemFromId(id: String): YtmMediaItem = when (this) {
            SONG -> YtmSong(YtmSong.cleanId(id))
            ARTIST -> YtmArtist(id)
            PLAYLIST -> YtmPlaylist(YtmPlaylist.cleanId(id))
        }

        companion object {
            fun fromBrowseEndpointType(page_type: String): Type {
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

fun <T: YtmMediaItem> T.copyWithName(name: String?): T =
    when (val item = this as YtmMediaItem) {
        is YtmSong -> item.copy(name = name) as T
        is YtmArtist -> item.copy(name = name) as T
        is YtmPlaylist -> item.copy(name = name) as T
    }
