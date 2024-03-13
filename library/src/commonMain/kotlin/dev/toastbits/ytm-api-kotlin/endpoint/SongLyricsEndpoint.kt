package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.YoutubeApi

abstract class SongLyricsEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getSongLyrics(lyrics_id: String): Result<String>
}
