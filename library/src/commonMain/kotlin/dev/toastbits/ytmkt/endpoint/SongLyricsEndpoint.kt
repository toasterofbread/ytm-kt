package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint

abstract class SongLyricsEndpoint: ApiEndpoint() {
    abstract suspend fun getSongLyrics(lyrics_id: String): Result<String>
}
