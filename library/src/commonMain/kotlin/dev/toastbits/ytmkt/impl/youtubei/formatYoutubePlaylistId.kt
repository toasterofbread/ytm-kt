package dev.toastbits.ytmkt.impl.youtubei

internal fun formatYoutubePlaylistId(playlist_id: String): String =
    playlist_id.removePrefix("VL")
