package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.uistrings.UiString
import dev.toastbits.ytmapi.model.external.YoutubePage

data class MediaItemLayout(
    val items: List<MediaItem>,
    val title: UiString?,
    val subtitle: UiString?,
    val type: Type? = null,
    var view_more: YoutubePage? = null
) {
    enum class Type {
        GRID,
        GRID_ALT,
        ROW,
        LIST,
        NUMBERED_LIST,
        CARD
    }
}
