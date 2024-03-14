package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ItemLayoutType
import dev.toastbits.ytmapi.model.external.YoutubePage
import dev.toastbits.ytmapi.uistrings.UiString

data class ArtistLayout(
    val items: List<MediaItem>? = null,
    val title: UiString? = null,
    val subtitle: UiString? = null,
    val type: ItemLayoutType? = null,
    val view_more: YoutubePage? = null,
    val playlist_id: String? = null
)
