package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.Serializable
import dev.toastbits.ytmkt.model.internal.TextRuns
import dev.toastbits.ytmkt.model.internal.MusicThumbnailRenderer
import dev.toastbits.ytmkt.model.internal.TextRun
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.ThumbnailProvider

@Serializable
data class ItemSectionRenderer(val contents: List<ItemSectionRendererContent>) {
    fun getMediaItems(): List<YtmMediaItem> {
        val items: MutableList<YtmMediaItem> = mutableListOf()
        for (content in contents) {
            for (item in content.playlistVideoListRenderer?.contents ?: emptyList()) {
                val song: YtmSong = item.playlistVideoRenderer.getSong() ?: continue
                items.add(song)
            }
            content.videoRenderer?.getSong()?.also { items.add(it) }
        }
        return items
    }
}

@Serializable
data class ItemSectionRendererContent(
    val didYouMeanRenderer: DidYouMeanRenderer?,
    val playlistVideoListRenderer: PlaylistVideoListRenderer?,
    val videoRenderer: VideoRenderer?
)

@Serializable
data class DidYouMeanRenderer(val correctedQuery: TextRuns)

@Serializable
data class PlaylistVideoListRenderer(
    val contents: List<Item>
) {
    @Serializable
    data class Item(val playlistVideoRenderer: VideoRenderer)
}

@Serializable
data class VideoRenderer(
    val videoId: String,
    val title: TextRuns,
    val shortBylineText: TextRuns?,
    val longBylineText: TextRuns?,
    val lengthSeconds: Long?,
    val isPlayable: Boolean?,
    val thumbnail: MusicThumbnailRenderer.RendererThumbnail
) {
    fun getSong(): YtmSong? {
        if (isPlayable == false) {
            return null
        }

        val artist_run: TextRun? =
            (shortBylineText?.runs.orEmpty() + longBylineText?.runs.orEmpty()).firstOrNull { it.navigationEndpoint?.commandMetadata?.webCommandMetadata?.webPageType == "WEB_PAGE_TYPE_CHANNEL" }

        val artist_name: String? = artist_run?.text
        val artist_id: String? = artist_run?.navigationEndpoint?.browseEndpoint?.browseId

        return YtmSong(
            id = videoId,
            name = title.firstTextOrNull(),
            duration = lengthSeconds?.times(1000L),
            thumbnail_provider = ThumbnailProvider.fromThumbnails(thumbnail.thumbnails),
            artists = artist_id?.let { id ->
                listOf(
                    YtmArtist(
                        id = id,
                        name = artist_name
                    )
                )
            }
        )
    }
}
