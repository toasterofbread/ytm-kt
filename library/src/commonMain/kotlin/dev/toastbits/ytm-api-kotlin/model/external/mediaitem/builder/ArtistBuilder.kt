package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ThumbnailProvider

class ArtistBuilder(
    override var id: String
): MediaItemBuilder() {
    override var name: String? = null
    override var thumbnail_provider: ThumbnailProvider? = null
    var shuffle_playlist_id: String? = null
    var layouts: List<ArtistLayout>? = null
    var subscribe_channel_id: String? = null
    var subscriber_count: Int? = null
    var subscribed: Boolean? = null

    override fun build(): Artist =
        Artist(
            id,
            name = name,
            thumbnail_provider = thumbnail_provider,
            shuffle_playlist_id = shuffle_playlist_id,
            layouts = layouts,
            subscribe_channel_id = subscribe_channel_id,
            subscriber_count = subscriber_count,
            subscribed = subscribed
        )
}
