package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.radio.RadioContinuation

class YtmPlaylistBuilder(
    override var id: String
): YtmMediaItemBuilder() {
    override var name: String? = null
    override var description: String? = null
    override var thumbnail_provider: ThumbnailProvider? = null
    var artist: YtmArtist? = null
    var type: YtmPlaylist.Type? = null
    var year: Int? = null
    var items: List<YtmSong>? = null
    var owner_id: String? = null
    var continuation: RadioContinuation? = null
    var item_set_ids: List<String>? = null
    var item_count: Int? = null
    var total_duration: Long? = null
    var playlist_url: String? = null

    override fun build(): YtmPlaylist =
        YtmPlaylist(
            id,
            name = name,
            description = description,
            thumbnail_provider = thumbnail_provider,
            artist = artist,
            type = type,
            year = year,
            items = items,
            owner_id = owner_id,
            continuation = continuation,
            item_set_ids = item_set_ids,
            item_count = item_count,
            total_duration = total_duration,
            playlist_url = playlist_url
        )
}
