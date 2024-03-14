package dev.toastbits.ytmapi.model.external

interface ThumbnailProvider {
    fun getThumbnailUrl(quality: Quality): String?
    override fun equals(other: Any?): Boolean

    enum class Quality {
        LOW, HIGH;

        fun getTargetSize(): Pair<Int, Int> {
            return when (this) {
                LOW -> Pair(180, 180)
                HIGH -> Pair(720, 720)
            }
        }

        companion object {
            fun byQuality(max: Quality = entries.last()): Iterable<Quality> =
                if (max == HIGH) listOf(HIGH, LOW)
                else listOf(LOW)
        }
    }

    companion object {
        fun fromImageUrl(url: String): ThumbnailProvider {
            return ThumbnailProviderImpl(url, null)
        }

        fun fromThumbnails(thumbnails: List<Thumbnail>): ThumbnailProvider? {
            if (thumbnails.isEmpty()) {
                return null
            }

            // Attempt to find dynamic thumbnail
            for (thumbnail in thumbnails) {
                val w_index = thumbnail.url.lastIndexOf("w${thumbnail.width}")
                if (w_index == -1) {
                    continue
                }

                val h_index = thumbnail.url.lastIndexOf("-h${thumbnail.height}")
                if (h_index == -1) {
                    continue
                }

                // Dynamic provider
                return ThumbnailProviderImpl(
                    thumbnail.url.substring(0, w_index + 1),
                    thumbnail.url.substring(h_index + 2 + thumbnail.height.toString().length)
                )
            }

            val high_url = thumbnails.maxByOrNull { it.width * it.height }!!.url
            val low_url = thumbnails.minByOrNull { it.width * it.height }!!.url

            // Set provider
            return ThumbnailProviderImpl(
                high_url,
                if (high_url == low_url) null else low_url
            )
        }
    }
}

data class ThumbnailProviderImpl(
    val url_a: String,
    val url_b: String?
): ThumbnailProvider {
    private fun isStatic(): Boolean {
        return url_b == null || url_b.startsWith("https://")
    }

    override fun getThumbnailUrl(quality: ThumbnailProvider.Quality): String? {
        // Set provider
        if (isStatic()) {
            if (url_b == null) {
                return url_a
            }

            return when (quality) {
                ThumbnailProvider.Quality.HIGH -> url_a
                ThumbnailProvider.Quality.LOW -> url_b
            }
        }

        // Dynamic provdier
        val (width, height) = quality.getTargetSize()
        return "$url_a${width}-h${height}$url_b"
    }
}
