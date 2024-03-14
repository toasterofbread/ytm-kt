package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.uistrings.parseYoutubeDurationString
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
    fun toMediaItem(hl: String): MediaItem {
        var album: Playlist? = null

        val podcast_text: TextRun? = secondTitle?.runs?.firstOrNull()
        if (podcast_text?.navigationEndpoint?.browseEndpoint?.browseId != null) {
            album = Playlist(
                podcast_text.navigationEndpoint.browseEndpoint.browseId,
                name = podcast_text.text
            )
        }

        if (album == null) {
            for (item in menu.menuRenderer.items) {
                val browse_endpoint: BrowseEndpoint = item.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint ?: continue
                if (browse_endpoint.browseId == null) {
                    continue
                }

                if (browse_endpoint.getPageType() == "MUSIC_PAGE_TYPE_PODCAST_SHOW_DETAIL_PAGE") {
                    album = Playlist(
                        browse_endpoint.browseId,
                        type = Playlist.Type.PODCAST
                    )
                    break
                }
                else if (browse_endpoint.getMediaItemType() == MediaItem.Type.PLAYLIST) {
                    album = Playlist(browse_endpoint.browseId)
                    break
                }
            }
        }

        var artist: Artist? = null
        for (run in subtitle.runs ?: emptyList()) {
            val browse_endpoint: BrowseEndpoint = run.navigationEndpoint?.browseEndpoint ?: continue
            if (browse_endpoint.browseId == null || browse_endpoint.getMediaItemType() != MediaItem.Type.ARTIST) {
                continue
            }

            artist = Artist(
                browse_endpoint.browseId,
                name = run.text
            )
            break
        }

        val first_title = title.runs!!.first()

        return Song(
            id = first_title.navigationEndpoint!!.browseEndpoint!!.browseId!!.removePrefix("MPED"),
            name = first_title.text,
            thumbnail_provider = thumbnail.toThumbnailProvider(),
            duration = subtitle.runs?.lastOrNull()?.text?.let { text ->
                parseYoutubeDurationString(text, hl)
            },
            type = if (onTap.getMusicVideoType() == "MUSIC_VIDEO_TYPE_PODCAST_EPISODE") Song.Type.PODCAST else null,
            artist = artist,
            album = album
        )
    }
}
