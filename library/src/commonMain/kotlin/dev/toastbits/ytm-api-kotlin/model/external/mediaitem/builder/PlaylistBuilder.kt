package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.radio.RadioContinuation

class PlaylistBuilder(
    override var id: String
): MediaItemBuilder() {
    override var name: String? = null
    override var thumbnail_provider: ThumbnailProvider? = null
    var artist: Artist? = null
    var type: Playlist.Type? = null
    var year: Int? = null
    var items: List<Song>? = null
    var owner_id: String? = null
    var continuation: RadioContinuation? = null
    var item_set_ids: List<String>? = null
    var item_count: Int? = null
    var total_duration: Long? = null

    override fun build(): Playlist =
        Playlist(
            id,
            name = name,
            thumbnail_provider = thumbnail_provider,
            artist = artist,
            type = type,
            year = year,
            items = items,
            owner_id = owner_id,
            continuation = continuation,
            item_set_ids = item_set_ids,
            item_count = item_count,
            total_duration = total_duration
        )
}
