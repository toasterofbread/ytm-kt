package dev.toastbits.ytmapi.model.external.mediaitem

data class Playlist(
    override val id: String,
    val items: List<Song> = emptyList(),
    val artist_id: String? = null
): MediaItem
