package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.copyWithName
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import kotlinx.serialization.Serializable

@Serializable
data class MusicCarouselShelfRenderer(
    override val header: Header,
    val contents: List<YoutubeiShelfContentsItem>
): YoutubeiHeaderContainer

@Serializable
data class MusicDescriptionShelfRenderer(val description: TextRuns, val header: TextRuns?)

@Serializable
data class MusicCardShelfRenderer(
    val thumbnail: ThumbnailRenderer,
    val title: TextRuns,
    val subtitle: TextRuns,
    val menu: YoutubeiNextResponse.Menu,
    override val header: Header
): YoutubeiHeaderContainer {
    fun getMediaItem(): YtmMediaItem {
        var item: YtmMediaItem
        val title_text: String = title.first_text

        val endpoint = title.runs!!.first().navigationEndpoint!!
        if (endpoint.watchEndpoint != null) {
            item = YtmSong(
                endpoint.watchEndpoint.videoId!!,
                name = title_text
            )
        }
        else {
            item = endpoint.browseEndpoint!!.getMediaItem()!!.copyWithName(title_text)
        }

        var artists: MutableList<YtmArtist> = mutableListOf()
        var album: YtmPlaylist? = null

        for (run in subtitle.runs ?: emptyList()) {
            val type = run.browse_endpoint_type ?: continue
            when (YtmMediaItem.Type.fromBrowseEndpointType(type)) {
                YtmMediaItem.Type.ARTIST -> {
                    val artist_item: YtmMediaItem? = run.navigationEndpoint?.browseEndpoint?.getMediaItem()
                    if (artist_item is YtmArtist) {
                        artists.add(artist_item)
                    }
                }
                YtmMediaItem.Type.PLAYLIST -> {
                    if (item !is YtmSong) {
                        continue
                    }

                    val album_item: YtmMediaItem? = run.navigationEndpoint?.browseEndpoint?.getMediaItem()
                    if (album_item is YtmPlaylist && album_item.type == YtmPlaylist.Type.ALBUM) {
                        album = album_item.copyWithName(run.text)
                    }
                }
                else -> {}
            }
        }

        if (artists.isEmpty() && item is YtmSong || item is YtmPlaylist) {
            artists.add(
                YtmArtist(
                    YtmArtist.getForItemId(item),
                    name = subtitle.runs?.getOrNull(1)?.text
                )
            )
        }

        val thumbnail_provider: ThumbnailProvider? = thumbnail.toThumbnailProvider()

        return when (item) {
            is YtmSong -> item.copy(artists = artists, thumbnail_provider = thumbnail_provider, album = album)
            is YtmPlaylist -> item.copy(artists = artists, thumbnail_provider = thumbnail_provider)
            is YtmArtist -> item.copy(thumbnail_provider = thumbnail_provider)
            else -> throw NotImplementedError(item::class.toString())
        }
    }
}
