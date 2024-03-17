package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider

class YtmArtistBuilder(
    override var id: String
): YtmMediaItemBuilder() {
    override var name: String? = null
    override var description: String? = null
    override var thumbnail_provider: ThumbnailProvider? = null
    var shuffle_playlist_id: String? = null
    var layouts: List<YtmArtistLayout>? = null
    var subscribe_channel_id: String? = null
    var subscriber_count: Int? = null
    var subscribed: Boolean? = null

    override fun build(): YtmArtist =
        YtmArtist(
            id,
            name = name,
            description = description,
            thumbnail_provider = thumbnail_provider,
            shuffle_playlist_id = shuffle_playlist_id,
            layouts = layouts,
            subscribe_channel_id = subscribe_channel_id,
            subscriber_count = subscriber_count,
            subscribed = subscribed
        )
}
