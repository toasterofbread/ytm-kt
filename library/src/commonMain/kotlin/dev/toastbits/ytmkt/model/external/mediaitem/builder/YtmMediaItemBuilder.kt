package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider

sealed class YtmMediaItemBuilder {
    abstract var id: String
    abstract var name: String?
    abstract var description: String?
    abstract var thumbnail_provider: ThumbnailProvider?

    abstract fun build(): YtmMediaItem
}
