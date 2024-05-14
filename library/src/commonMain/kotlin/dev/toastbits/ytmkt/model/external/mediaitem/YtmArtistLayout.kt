package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ItemLayoutType
import dev.toastbits.ytmkt.model.external.YoutubePage
import dev.toastbits.ytmkt.uistrings.UiString
import kotlinx.serialization.Serializable

@Serializable
data class YtmArtistLayout(
    val items: List<YtmMediaItem>? = null,
    val title: UiString? = null,
    val subtitle: UiString? = null,
    val type: ItemLayoutType? = null,
    val view_more: YoutubePage? = null,
    val playlist_id: String? = null
)
