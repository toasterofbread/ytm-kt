package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.copyWithName
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

data class MusicCarouselShelfRenderer(
    override val header: Header,
    val contents: List<YoutubeiShelfContentsItem>
): YoutubeiHeaderContainer

data class MusicDescriptionShelfRenderer(val description: TextRuns, val header: TextRuns?)

data class MusicCardShelfRenderer(
    val thumbnail: ThumbnailRenderer,
    val title: TextRuns,
    val subtitle: TextRuns,
    val menu: YoutubeiNextResponse.Menu,
    override val header: Header
): YoutubeiHeaderContainer {
    fun getMediaItem(): MediaItem {
        var item: MediaItem
        val title_text: String = title.first_text

        val endpoint = title.runs!!.first().navigationEndpoint!!
        if (endpoint.watchEndpoint != null) {
            item = Song(
                endpoint.watchEndpoint.videoId!!,
                name = title_text
            )
        }
        else {
            item = endpoint.browseEndpoint!!.getMediaItem()!!.copyWithName(title_text)
        }

        var artist: Artist? = null
        var album: Playlist? = null

        for (run in subtitle.runs ?: emptyList()) {
            val type = run.browse_endpoint_type ?: continue
            when (MediaItem.Type.fromBrowseEndpointType(type)) {
                MediaItem.Type.ARTIST -> {
                    val artist_item: MediaItem? = run.navigationEndpoint?.browseEndpoint?.getMediaItem()
                    if (artist_item is Artist) {
                        artist = artist_item

                        if (album != null) {
                            break
                        }
                    }
                }
                MediaItem.Type.PLAYLIST -> {
                    if (item !is Song) {
                        continue
                    }

                    val album_item: MediaItem? = run.navigationEndpoint?.browseEndpoint?.getMediaItem()
                    if (album_item is Playlist && album_item.type == Playlist.Type.ALBUM) {
                        album = album_item.copyWithName(run.text)

                        if (artist != null) {
                            break
                        }
                    }
                }
                else -> {}
            }
        }

        if (artist == null && item is Song || item is Playlist) {
            artist = Artist(
                Artist.getForItemId(item),
                name = subtitle.runs?.getOrNull(1)?.text
            )
        }

        val thumbnail_provider: ThumbnailProvider = thumbnail.toThumbnailProvider()

        return when (item) {
            is Song -> item.copy(artist = artist, thumbnail_provider = thumbnail_provider, album = album)
            is Playlist -> item.copy(artist = artist, thumbnail_provider = thumbnail_provider)
            is Artist -> item.copy(thumbnail_provider = thumbnail_provider)
        }
    }
}
