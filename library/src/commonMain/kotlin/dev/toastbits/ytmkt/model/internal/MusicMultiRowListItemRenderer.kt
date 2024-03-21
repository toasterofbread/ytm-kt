package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.uistrings.parseYoutubeDurationString
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import kotlinx.serialization.Serializable

@Serializable
class OnTap(
    val watchEndpoint: WatchEndpoint
) {
    @Serializable
    class WatchEndpoint(val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs)
    @Serializable
    class WatchEndpointMusicSupportedConfigs(val watchEndpointMusicConfig: WatchEndpointMusicConfig)
    @Serializable
    class WatchEndpointMusicConfig(val musicVideoType: String)

    fun getMusicVideoType(): String =
        watchEndpoint.watchEndpointMusicSupportedConfigs.watchEndpointMusicConfig.musicVideoType
}

@Serializable
class MusicMultiRowListItemRenderer(
    val title: TextRuns,
    val subtitle: TextRuns,
    val thumbnail: ThumbnailRenderer,
    val menu: YoutubeiNextResponse.Menu,
    val onTap: OnTap,
    val secondTitle: TextRuns?
) {
    fun toMediaItem(hl: String): YtmMediaItem {
        var album: YtmPlaylist? = null

        val podcast_text: TextRun? = secondTitle?.runs?.firstOrNull()
        if (podcast_text?.navigationEndpoint?.browseEndpoint?.browseId != null) {
            album = YtmPlaylist(
                YtmPlaylist.cleanId(podcast_text.navigationEndpoint.browseEndpoint.browseId),
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
                    album = YtmPlaylist(
                        YtmPlaylist.cleanId(browse_endpoint.browseId),
                        type = YtmPlaylist.Type.PODCAST
                    )
                    break
                }
                else if (browse_endpoint.getMediaItemType() == YtmMediaItem.Type.PLAYLIST) {
                    album = YtmPlaylist(YtmPlaylist.cleanId(browse_endpoint.browseId))
                    break
                }
            }
        }

        val artists: List<YtmArtist>? = subtitle.runs?.mapNotNull { run ->
            val browse_endpoint: BrowseEndpoint? = run.navigationEndpoint?.browseEndpoint
            if (browse_endpoint?.browseId == null || browse_endpoint.getMediaItemType() != YtmMediaItem.Type.ARTIST) {
                return@mapNotNull null
            }

            return@mapNotNull YtmArtist(
                browse_endpoint.browseId,
                name = run.text
            )
        }

        val first_title = title.runs!!.first()

        return YtmSong(
            id = YtmSong.cleanId(first_title.navigationEndpoint!!.browseEndpoint!!.browseId!!),
            name = first_title.text,
            thumbnail_provider = thumbnail.toThumbnailProvider(),
            duration = subtitle.runs?.lastOrNull()?.text?.let { text ->
                parseYoutubeDurationString(text, hl)
            },
            type = if (onTap.getMusicVideoType() == "MUSIC_VIDEO_TYPE_PODCAST_EPISODE") YtmSong.Type.PODCAST else null,
            artists = artists,
            album = album
        )
    }
}
