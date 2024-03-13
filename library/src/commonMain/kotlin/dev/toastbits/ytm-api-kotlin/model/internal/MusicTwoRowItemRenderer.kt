package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.enums.MediaItemType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.PlaylistType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.SongType
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylist
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import com.toasterofbread.spmp.model.settings.Settings
import com.toasterofbread.spmp.model.settings.category.FeedSettings
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint,
    val title: TextRuns,
    val subtitle: TextRuns?,
    val thumbnailRenderer: ThumbnailRenderer,
    val menu: YoutubeiNextResponse.Menu?,
    val subtitleBadges: List<MusicResponsiveListItemRenderer.Badge>?
) {
    private fun getArtist(host_item: MediaItemData): Artist? {
        for (run in subtitle?.runs ?: emptyList()) {
            val browse_endpoint: BrowseEndpoint? = run.navigationEndpoint?.browseEndpoint
            if (browse_endpoint?.browseId == null) {
                continue
            }

            if (browse_endpoint.getMediaItemType() == MediaItemType.ARTIST) {
                return ArtistData(browse_endpoint.browseId).apply {
                    title = run.text
                }
            }
        }

        if (host_item is SongData) {
            val index = if (host_item.song_type == SongType.VIDEO) 0 else 1
            subtitle?.runs?.getOrNull(index)?.also {
                return ArtistData(Artist.getForItemId(host_item)).apply {
                    title = it.text
                }
            }
        }

        return null
    }
    
    fun toMediaItem(hl: String): MediaItemData? {
        // Video
        if (navigationEndpoint.watchEndpoint?.videoId != null) {
            val first_thumbnail = thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.first()
            return SongData(navigationEndpoint.watchEndpoint.videoId).also { data ->
                data.song_type = if (first_thumbnail.height == first_thumbnail.width) SongType.SONG else SongType.VIDEO
                data.title = this@MusicTwoRowItemRenderer.title.first_text
                data.thumbnail_provider = thumbnailRenderer.toThumbnailProvider()
                data.artist = getArtist(data)
                data.explicit = subtitleBadges?.any { it.isExplicit() } == true

                for (item in menu?.menuRenderer?.items ?: emptyList()) {
                    val browse_endpoint: BrowseEndpoint = item.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint ?: continue
                    if (browse_endpoint.browseId != null && browse_endpoint.getMediaItemType() == MediaItemType.PLAYLIST_REM) {
                        data.album = Playlist(browse_endpoint.browseId)
                        break
                    }
                }
            }
        }

        val item: MediaItemData

        if (navigationEndpoint.watchPlaylistEndpoint != null) {
            if (!Settings.get<Boolean>(FeedSettings.Key.SHOW_RADIOS)) {
                return null
            }

            item = Playlist(navigationEndpoint.watchPlaylistEndpoint.playlistId).also { data ->
                data.playlist_type = PlaylistType.RADIO
                data.title = title.first_text
                data.thumbnail_provider = thumbnailRenderer.toThumbnailProvider()
            }
        }
        else {
            // Playlist or artist
            val browse_id: String = navigationEndpoint.browseEndpoint?.browseId ?: return null
            val page_type: String = navigationEndpoint.browseEndpoint.getPageType() ?: return null

            item = when (MediaItemType.fromBrowseEndpointType(page_type)) {
                MediaItemType.SONG -> SongData(browse_id)
                MediaItemType.ARTIST -> ArtistData(browse_id)
                MediaItemType.PLAYLIST_REM -> {
                    if (RemotePlaylist.formatYoutubeId(browse_id).startsWith("RDAT") && !Settings.get<Boolean>(FeedSettings.Key.SHOW_RADIOS)) {
                        return null
                    }

                    Playlist(browse_id).also { data ->
                        data.playlist_type = PlaylistType.fromBrowseEndpointType(page_type)
                        data.artist = getArtist(data)
//                        is_editable = menu?.menuRenderer?.items
//                            ?.any { it.menuNavigationItemRenderer?.icon?.iconType == "DELETE" } == true
                    }
                }
                MediaItemType.PLAYLIST_LOC -> throw IllegalStateException("$page_type ($browse_id)")
            }

            item.title = title.first_text
            item.thumbnail_provider = thumbnailRenderer.toThumbnailProvider()
        }

        return item
    }
}
