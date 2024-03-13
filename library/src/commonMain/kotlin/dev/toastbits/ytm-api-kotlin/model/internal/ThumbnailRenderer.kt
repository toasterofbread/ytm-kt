package dev.toastbits.ytmapi.model.internal


data class Thumbnails(val musicThumbnailRenderer: MusicThumbnailRenderer?, val croppedSquareThumbnailRenderer: MusicThumbnailRenderer?) {
    init {
        assert(musicThumbnailRenderer != null || croppedSquareThumbnailRenderer != null)
    }
    val thumbnails: List<Thumbnail> get() = (musicThumbnailRenderer ?: croppedSquareThumbnailRenderer!!).thumbnail.thumbnails
}

data class MusicThumbnailRenderer(val thumbnail: RendererThumbnail) {
    data class RendererThumbnail(val thumbnails: List<Thumbnail>)
}

data class ThumbnailRenderer(val musicThumbnailRenderer: MusicThumbnailRenderer)
