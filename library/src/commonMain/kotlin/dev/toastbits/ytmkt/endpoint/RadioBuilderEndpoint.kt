package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.Thumbnail

abstract class RadioBuilderEndpoint: ApiEndpoint() {
    abstract suspend fun getRadioBuilderArtists(
        selectThumbnail: (List<Thumbnail>) -> Thumbnail
    ): Result<List<RadioBuilderArtist>>

    abstract fun buildRadioToken(artists: Set<RadioBuilderArtist>, modifiers: Set<RadioBuilderModifier?>): String

    abstract suspend fun getBuiltRadio(radio_token: String): Result<YtmPlaylist?>
}

data class RadioBuilderArtist(
    val name: String,
    val token: String,
    val thumbnail: Thumbnail
)

sealed interface RadioBuilderModifier {
    val string: String?

    enum class Internal: RadioBuilderModifier {
        ARTIST;

        override val string: String? get() = throw IllegalStateException()
    }

    enum class Variety: RadioBuilderModifier {
        LOW, MEDIUM, HIGH;
        override val string: String? get() = when (this) {
            LOW -> "rX"
            MEDIUM -> null
            HIGH -> "rZ"
        }
    }

    enum class SelectionType: RadioBuilderModifier {
        FAMILIAR, BLEND, DISCOVER;
        override val string: String? get() = when (this) {
            FAMILIAR -> "iY"
            BLEND -> null
            DISCOVER -> "iX"
        }
    }

    enum class FilterA: RadioBuilderModifier {
        POPULAR, HIDDEN, NEW;
        override val string: String? get() = when (this) {
            POPULAR -> "pY"
            HIDDEN -> "pX"
            NEW -> "dX"
        }
    }

    enum class FilterB: RadioBuilderModifier {
        PUMP_UP, CHILL, UPBEAT, DOWNBEAT, FOCUS;
        override val string: String? get() = when (this) {
            PUMP_UP -> "mY"
            CHILL -> "mX"
            UPBEAT -> "mb"
            DOWNBEAT -> "mc"
            FOCUS -> "ma"
        }
    }

    companion object {
        fun fromString(modifier: String): RadioBuilderModifier? {
            return when (modifier) {
                "iY" -> SelectionType.FAMILIAR
                "iX" -> SelectionType.DISCOVER
                "pY" -> FilterA.POPULAR
                "pX" -> FilterA.HIDDEN
                "dX" -> FilterA.NEW
                "mY" -> FilterB.PUMP_UP
                "mX" -> FilterB.CHILL
                "mb" -> FilterB.UPBEAT
                "mc" -> FilterB.DOWNBEAT
                "ma" -> FilterB.FOCUS
                "rX" -> Variety.LOW
                "rZ" -> Variety.HIGH
                else -> null
            }
        }
    }
}
