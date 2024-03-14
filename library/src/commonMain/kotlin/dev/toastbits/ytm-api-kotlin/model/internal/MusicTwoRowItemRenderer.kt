package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.itemcache.MediaItemCache

class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint,
    val title: TextRuns,
    val subtitle: TextRuns?,
    val thumbnailRenderer: ThumbnailRenderer,
    val menu: YoutubeiNextResponse.Menu?,
    val subtitleBadges: List<MusicResponsiveListItemRenderer.Badge>?
) {
    private fun getArtist(host_item: MediaItem, api: YoutubeApi): Artist? {
        for (run in subtitle?.runs ?: emptyList()) {
            val browse_endpoint: BrowseEndpoint? = run.navigationEndpoint?.browseEndpoint
            if (browse_endpoint?.browseId == null) {
                continue
            }

            if (browse_endpoint.getMediaItemType() == MediaItem.Type.ARTIST) {
                return Artist(
                    browse_endpoint.browseId,
                    name = run.text
                )
            }
        }

        if (host_item is Song) {
            val song_type: Song.Type? = api.item_cache.getSong(
                host_item.id,
                setOf(MediaItemCache.SongKey.TYPE)
            )?.type

            val index: Int = if (song_type == Song.Type.VIDEO) 0 else 1
            subtitle?.runs?.getOrNull(index)?.also {
                return Artist(Artist.getForItemId(host_item)).copy(
                    name = it.text
                )
            }
        }

        return null
    }

    fun toMediaItem(api: YoutubeApi): MediaItem? {
        // Video
        if (navigationEndpoint.watchEndpoint?.videoId != null) {
            var album: Playlist? = null
            for (item in menu?.menuRenderer?.items ?: emptyList()) {
                val browse_endpoint: BrowseEndpoint = item.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint ?: continue
                if (browse_endpoint.browseId != null && browse_endpoint.getMediaItemType() == MediaItem.Type.PLAYLIST) {
                    album = Playlist(browse_endpoint.browseId)
                    break
                }
            }

            val first_thumbnail = thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.first()
            val song_id: String = navigationEndpoint.watchEndpoint.videoId

            return Song(
                id = song_id,
                type = if (first_thumbnail.height == first_thumbnail.width) Song.Type.SONG else Song.Type.VIDEO,
                name = this@MusicTwoRowItemRenderer.title.first_text,
                thumbnail_provider = thumbnailRenderer.toThumbnailProvider(),
                artist = getArtist(Song(song_id), api),
                is_explicit = subtitleBadges?.any { it.isExplicit() } == true,
                album = album
            )
        }

        val item: MediaItem

        if (navigationEndpoint.watchPlaylistEndpoint != null) {
            item = Playlist(
                id = navigationEndpoint.watchPlaylistEndpoint.playlistId,
                type = Playlist.Type.RADIO,
                name = title.first_text,
                thumbnail_provider = thumbnailRenderer.toThumbnailProvider()
            )
        }
        else {
            // Playlist or artist
            val browse_id: String = navigationEndpoint.browseEndpoint?.browseId ?: return null
            val page_type: String = navigationEndpoint.browseEndpoint.getPageType() ?: return null

            val title: String = title.first_text
            val thumbnail_provider: ThumbnailProvider = thumbnailRenderer.toThumbnailProvider()

            item = when (MediaItem.Type.fromBrowseEndpointType(page_type)) {
                MediaItem.Type.SONG ->
                    Song(
                        browse_id,
                        name = title,
                        thumbnail_provider = thumbnail_provider
                    )
                MediaItem.Type.ARTIST ->
                    Artist(
                        browse_id,
                        name = title,
                        thumbnail_provider = thumbnail_provider
                    )
                MediaItem.Type.PLAYLIST ->
                    Playlist(
                        browse_id,
                        type = Playlist.Type.fromBrowseEndpointType(page_type),
                        artist = getArtist(Playlist(browse_id), api),
                        name = title,
                        thumbnail_provider = thumbnail_provider
                    )
            }
        }

        return item
    }
}
