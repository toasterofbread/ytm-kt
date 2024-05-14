package dev.toastbits.ytmkt.model.external.mediaitem

import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class YtmSong(
    override val id: String,
    override val name: String? = null,
    override val description: String? = null,
    override val thumbnail_provider: ThumbnailProvider? = null,
    val artists: List<YtmArtist>? = null,
    @SerialName("song_type")
    val type: Type? = null,
    val is_explicit: Boolean = false,
    val album: YtmPlaylist? = null,
    val duration: Long? = null,
    val related_browse_id: String? = null,
    val lyrics_browse_id: String? = null
): YtmMediaItem {
    init {
        check(id == cleanId(id)) { "Song ID ($id) was not cleaned before YtmSong creation" }
    }

    enum class Type {
        SONG, VIDEO, PODCAST
    }

    companion object {
        fun cleanId(id: String): String = id.removePrefix("MPED")
    }
}
