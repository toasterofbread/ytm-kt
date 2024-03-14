package dev.toastbits.ytmapi.impl.youtubemusic

internal fun formatYoutubePlaylistId(playlist_id: String): String =
    playlist_id.removePrefix("VL")
