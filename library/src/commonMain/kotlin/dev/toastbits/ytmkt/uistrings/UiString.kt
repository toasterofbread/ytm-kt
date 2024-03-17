package dev.toastbits.ytmkt.uistrings

import dev.toastbits.ytmkt.uistrings.localised.getByLanguage
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem

interface UiString {
    fun getString(language: String): String
    companion object
}

data class RawUiString(
    val raw_string: String
): UiString {
    override fun getString(language: String): String = raw_string
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

    fun getYoutubeStringId(): YoutubeUILocalisation.StringID? =
        type.getStringData().item_ids[index]

    private var localised: Pair<String, String?>? = null
    private fun getLocalised(language: String): Pair<String, String?> {
        val strings: YoutubeUILocalisation.LocalisationSet = type.getStringData()

        val item: Map<String, Pair<String, String?>>? = strings.items.getOrNull(index)
        if (item == null) {
            throw RuntimeException("Could not get localised string item ($index, ${strings.items.toList()})")
        }

        return getLocalisationSetItemString(language, item)
    }

    private fun getLocalisationSetItemString(
        language: String,
        item: Map<String, Pair<String, String?>>
    ): Pair<String, String?> =
        try {
            item.getByLanguage(language)!!.value.value
        }
        catch (e: Throwable) {
            item["en-GB"] ?: item.values.first()
        }

    companion object {
        fun mediaItemPage(key: String, item_type: YtmMediaItem.Type, language: String): UiString =
            when (item_type) {
                YtmMediaItem.Type.ARTIST -> Type.ARTIST_PAGE.createFromKey(key, language)
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
