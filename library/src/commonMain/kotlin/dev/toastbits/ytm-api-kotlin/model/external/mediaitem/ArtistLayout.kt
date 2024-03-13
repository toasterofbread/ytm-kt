package dev.toastbits.ytmapi.model.external.mediaitem

import dev.toastbits.ytmapi.model.external.ItemLayoutType

data class ArtistLayout(
    val items: List<MediaItem>? = null,
    val title: LocalisedString? = null,
    val subtitle: LocalisedString? = null,
    val type: ItemLayoutType? = null,
    val view_more: ViewMore? = null,
    val playlist_id: String? = null
)
