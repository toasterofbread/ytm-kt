package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song

class SongBuilder(
    override var id: String
): MediaItemBuilder() {
    override var name: String? = null
    override var thumbnail_provider: ThumbnailProvider? = null
    var artist: Artist? = null
    var type: Song.Type? = null
    var is_explicit: Boolean = false
    var album: Playlist? = null
    var duration: Long? = null
    var related_browse_id: String? = null
    var lyrics_browse_id: String? = null

    override fun build(): Song =
        Song(
            id,
            name = name,
            thumbnail_provider = thumbnail_provider,
            artist = artist,
            type = type,
            is_explicit = is_explicit,
            album = album,
            duration = duration,
            related_browse_id = related_browse_id,
            lyrics_browse_id = lyrics_browse_id
        )
}
