package dev.toastbits.ytmapi.uistrings

import dev.toastbits.ytmapi.uistrings.localised.getByLanguage
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem

sealed interface UiString {
    fun getString(language: String): String
    fun getType(): Type

    fun serialise(): String {
        val data: String = when (this) {
            is RawUiString -> raw_string
            is AppUiString -> string_key
            is YoutubeUiString -> "${type.ordinal},$index"
        }

        return "${getType().ordinal},$data"
    }

    enum class Type {
        RAW,
        APP,
        YOUTUBE;
    }

    companion object {
        fun deserialise(data: String): UiString {
            val split: List<String> = data.split(",", limit = 2)

            try {
                val type = Type.entries[split[0].toInt()]

                when (type) {
                    Type.RAW -> return RawUiString(split[1])
                    Type.APP -> return AppUiString(split[1])
                    Type.YOUTUBE -> {
                        if (split.size < 2) {
                            return RawUiString("")
                        }

                        val (youtube_type_index, index) = split[1].split(",", limit = 2)
                        return YoutubeUiString(
                            YoutubeUiString.Type.entries[youtube_type_index.toInt()],
                            index.toInt()
                        )
                    }
                }
            }
            catch (e: Throwable) {
                throw RuntimeException("UiString deserialisation failed '$data' $split", e)
            }
        }
    }
}

data class RawUiString(
    val raw_string: String
): UiString {
    override fun getString(language: String): String = raw_string
    override fun getType(): UiString.Type = UiString.Type.RAW
}

data class AppUiString(
    val string_key: String
): UiString {
    override fun getString(language: String): String = getString(string_key)
    override fun getType(): UiString.Type = UiString.Type.APP
}

data class YoutubeUiString(
    val type: Type,
    val index: Int
): UiString {
    enum class Type {
        HOME_FEED,
        OWN_CHANNEL,
        ARTIST_PAGE,
        SEARCH_PAGE,
        FILTER_CHIP;

        fun createFromKey(key: String, source_language: String): UiString {
            val strings: YoutubeUILocalisation.LocalisationSet = getStringData()

            for ((index, item) in strings.items.withIndex()) {
                val by_language = item.getByLanguage(source_language)
                if (by_language?.value?.value?.first == key) {
                    return YoutubeUiString(this, index)
                }
            }
            return RawUiString(key)
        }
    }

    override fun getString(language: String): String = getLocalised(language).let { it.second ?: it.first }
    override fun getType(): UiString.Type = UiString.Type.YOUTUBE

    fun getYoutubeStringId(): YoutubeUILocalisation.StringID? =
        type.getStringData().item_ids[index]

    private var localised: Pair<String, String?>? = null
    private fun getLocalised(language: String): Pair<String, String?> {
        if (localised == null) {
            val strings: YoutubeUILocalisation.LocalisationSet = type.getStringData()

            val item: Map<String, Pair<String, String?>>? = strings.items.getOrNull(index)
            if (item == null) {
                throw RuntimeException("Could not get localised string item ($index, ${strings.items.toList()})")
            }

            localised = getLocalisationSetItemString(language, item)
        }

        return localised!!
    }

    private fun getLocalisationSetItemString(
        language: String,
        item: Map<String, Pair<String, String?>>
    ): Pair<String, String?> =
        try {
            item.getByLanguage(language)!!.value.value
        }
        catch (e: Throwable) {
            throw RuntimeException("Could not get localised string ($index, $language, $item)", e)
        }

    companion object {
        fun mediaItemPage(key: String, item_type: MediaItem.Type, language: String): UiString =
            when (item_type) {
                MediaItem.Type.ARTIST -> Type.ARTIST_PAGE.createFromKey(key, language)
                else -> RawUiString(key)
            }
    }
}

private fun YoutubeUiString.Type.getStringData(): YoutubeUILocalisation.LocalisationSet =
    when (this) {
        YoutubeUiString.Type.HOME_FEED -> YoutubeUILocalisation.HOME_FEED_STRINGS
        YoutubeUiString.Type.OWN_CHANNEL -> YoutubeUILocalisation.OWN_CHANNEL_STRINGS
        YoutubeUiString.Type.ARTIST_PAGE -> YoutubeUILocalisation.ARTIST_PAGE_STRINGS
        YoutubeUiString.Type.SEARCH_PAGE -> YoutubeUILocalisation.SEARCH_PAGE_STRINGS
        YoutubeUiString.Type.FILTER_CHIP -> YoutubeUILocalisation.FILTER_CHIPS
    }
