package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ThumbnailProvider

sealed class MediaItemBuilder {
    abstract var id: String
    abstract var name: String?
    abstract var thumbnail_provider: ThumbnailProvider?

    abstract fun build(): MediaItem
}
