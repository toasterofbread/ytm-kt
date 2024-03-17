package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.Thumbnail
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import kotlinx.serialization.Serializable

@Serializable
data class Thumbnails(val musicThumbnailRenderer: MusicThumbnailRenderer?, val croppedSquareThumbnailRenderer: MusicThumbnailRenderer?) {
    val thumbnails: List<Thumbnail> get() = (musicThumbnailRenderer ?: croppedSquareThumbnailRenderer!!).thumbnail.thumbnails
}

@Serializable
data class MusicThumbnailRenderer(val thumbnail: RendererThumbnail) {
    @Serializable
    data class RendererThumbnail(val thumbnails: List<Thumbnail>)
}

@Serializable
data class ThumbnailRenderer(val musicThumbnailRenderer: MusicThumbnailRenderer?) {
    fun toThumbnailProvider(): ThumbnailProvider? {
        return ThumbnailProvider.fromThumbnails(musicThumbnailRenderer?.thumbnail?.thumbnails ?: emptyList())
    }
}
