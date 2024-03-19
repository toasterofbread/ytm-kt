package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider

class YtmSongBuilder(
    override var id: String
): YtmMediaItemBuilder() {
    override var name: String? = null
    override var description: String? = null
    override var thumbnail_provider: ThumbnailProvider? = null
    var artists: List<YtmArtist>? = null
    var type: YtmSong.Type? = null
    var is_explicit: Boolean = false
    var album: YtmPlaylist? = null
    var duration: Long? = null
    var related_browse_id: String? = null
    var lyrics_browse_id: String? = null

    override fun build(): YtmSong =
        YtmSong(
            id,
            name = name,
            description = description,
            thumbnail_provider = thumbnail_provider,
            artists = artists,
            type = type,
            is_explicit = is_explicit,
            album = album,
            duration = duration,
            related_browse_id = related_browse_id,
            lyrics_browse_id = lyrics_browse_id
        )
}
