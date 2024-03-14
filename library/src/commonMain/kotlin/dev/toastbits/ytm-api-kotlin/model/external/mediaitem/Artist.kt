package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.mediaitem.ArtistLayout
import dev.toastbits.ytmapi.model.external.ThumbnailProvider

data class Artist(
    override val id: String,
    override val name: String? = null,
    override val thumbnail_provider: ThumbnailProvider? = null,
    val shuffle_playlist_id: String? = null,
    val layouts: List<ArtistLayout>? = null,
    val subscribe_channel_id: String? = null,
    val subscriber_count: Int? = null,
    val subscribed: Boolean? = null
): MediaItem() {
    companion object {
        fun getForItemId(item: MediaItem): String {
            val prefix: String =
                when (item) {
                    is Song -> "FORSONG"
                    is Playlist -> "FORPLAYLIST"
                    is Artist -> "FORARTIST"
                }
            return prefix + item.id
        }
    }
}
