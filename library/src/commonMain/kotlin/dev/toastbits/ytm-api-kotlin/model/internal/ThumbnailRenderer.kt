package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.Thumbnail
import dev.toastbits.ytmapi.model.external.ThumbnailProvider

data class Thumbnails(val musicThumbnailRenderer: MusicThumbnailRenderer?, val croppedSquareThumbnailRenderer: MusicThumbnailRenderer?) {
    val thumbnails: List<Thumbnail> get() = (musicThumbnailRenderer ?: croppedSquareThumbnailRenderer!!).thumbnail.thumbnails
}

data class MusicThumbnailRenderer(val thumbnail: RendererThumbnail) {
    data class RendererThumbnail(val thumbnails: List<Thumbnail>)
}

data class ThumbnailRenderer(val musicThumbnailRenderer: MusicThumbnailRenderer) {
    fun toThumbnailProvider(): ThumbnailProvider {
        return ThumbnailProvider.fromThumbnails(musicThumbnailRenderer.thumbnail.thumbnails)!!
    }
}
