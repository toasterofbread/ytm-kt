package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.enums.MediaItemType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.PlaylistType
import dev.toastbits.ytmapi.model.external.mediaitem.enums.SongType
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import com.toasterofbread.spmp.resources.uilocalisation.parseYoutubeDurationString
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

class OnTap(
    val watchEndpoint: WatchEndpoint
) {
    class WatchEndpoint(val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs)
    class WatchEndpointMusicSupportedConfigs(val watchEndpointMusicConfig: WatchEndpointMusicConfig)
    class WatchEndpointMusicConfig(val musicVideoType: String)

    fun getMusicVideoType(): String =
        watchEndpoint.watchEndpointMusicSupportedConfigs.watchEndpointMusicConfig.musicVideoType
}

class MusicMultiRowListItemRenderer(
    val title: TextRuns,
    val subtitle: TextRuns,
    val thumbnail: ThumbnailRenderer,
    val menu: YoutubeiNextResponse.Menu,
    val onTap: OnTap,
    val secondTitle: TextRuns?
) {
    fun toMediaItem(hl: String): MediaItemData {
        val title = title.runs!!.first()
        return SongData(
            title.navigationEndpoint!!.browseEndpoint!!.browseId!!.removePrefix("MPED")
        ).also { song ->
            song.title = title.text
            song.thumbnail_provider = thumbnail.toThumbnailProvider()

            song.duration = subtitle.runs?.lastOrNull()?.text?.let { text ->
                parseYoutubeDurationString(text, hl)
            }

            if (onTap.getMusicVideoType() == "MUSIC_VIDEO_TYPE_PODCAST_EPISODE") {
                song.song_type = SongType.PODCAST
            }

            var podcast_data: Playlist? = null

            val podcast_text: TextRun? = secondTitle?.runs?.firstOrNull()
            if (podcast_text?.navigationEndpoint?.browseEndpoint?.browseId != null) {
                podcast_data = Playlist(
                    podcast_text.navigationEndpoint.browseEndpoint.browseId
                ).also { data ->
                    data.title = podcast_text.text
                }
            }

            for (item in menu.menuRenderer.items) {
                val browse_endpoint: BrowseEndpoint = item.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint ?: continue
                if (browse_endpoint.browseId == null) {
                    continue
                }

                if (podcast_data == null && browse_endpoint.getPageType() == "MUSIC_PAGE_TYPE_PODCAST_SHOW_DETAIL_PAGE") {
                    podcast_data = Playlist(browse_endpoint.browseId)
                }
                else if (browse_endpoint.getMediaItemType() == MediaItemType.PLAYLIST_REM) {
                    song.album = Playlist(browse_endpoint.browseId)
                }
            }

            if (podcast_data != null) {
                podcast_data.playlist_type = PlaylistType.PODCAST
                song.album = podcast_data
            }

            for (run in subtitle.runs ?: emptyList()) {
                val browse_endpoint: BrowseEndpoint = run.navigationEndpoint?.browseEndpoint ?: continue
                if (browse_endpoint.browseId == null || browse_endpoint.getMediaItemType() != MediaItemType.ARTIST) {
                    continue
                }

                song.artist = ArtistData(browse_endpoint.browseId)
                    .also { artist ->
                        artist.title = run.text
                    }
            }
        }
    }
}
