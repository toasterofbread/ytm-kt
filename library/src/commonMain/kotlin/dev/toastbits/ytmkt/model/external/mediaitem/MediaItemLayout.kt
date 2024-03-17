package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ItemLayoutType
import dev.toastbits.ytmkt.uistrings.UiString
import dev.toastbits.ytmkt.model.external.YoutubePage

data class MediaItemLayout(
    val items: List<YtmMediaItem>,
    val title: UiString?,
    val subtitle: UiString?,
    val type: ItemLayoutType? = null,
    val view_more: YoutubePage? = null
)
