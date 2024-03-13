package dev.toastbits.ytmapi

import dev.toastbits.ytmapi.impl.youtubemusic.RelatedGroup

abstract class SongRelatedContentEndpoint: YoutubeApi.Endpoint() {
    abstract suspend fun getSongRelated(song_id: String): Result<List<RelatedGroup>>
}