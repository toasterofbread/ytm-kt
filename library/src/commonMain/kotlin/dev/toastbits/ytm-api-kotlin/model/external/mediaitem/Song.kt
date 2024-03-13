package dev.toastbits.ytmapi.model.external.mediaitem

data class Song(
    override val id: String,
    val artist_id: String? = null,
    val title: String? = null,
    val related_browse_id: String? = null,
    val lyrics_browse_id: String? = null,
    val is_explicit: Boolean = false
): MediaItem
