package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider

data class YtmArtist(
    override val id: String,
    override val name: String? = null,
    override val description: String? = null,
    override val thumbnail_provider: ThumbnailProvider? = null,
    val shuffle_playlist_id: String? = null,
    val layouts: List<YtmArtistLayout>? = null,
    val subscribe_channel_id: String? = null,
    val subscriber_count: Int? = null,
    val subscribed: Boolean? = null
): YtmMediaItem {
    companion object {
        fun getForItemId(item: YtmMediaItem): String {
            val prefix: String =
                when (item) {
                    is YtmSong -> "FORSONG"
                    is YtmPlaylist -> "FORPLAYLIST"
                    is YtmArtist -> "FORARTIST"
                    else -> throw NotImplementedError(item::class.toString())
                }
            return prefix + item.id
        }
    }
}
