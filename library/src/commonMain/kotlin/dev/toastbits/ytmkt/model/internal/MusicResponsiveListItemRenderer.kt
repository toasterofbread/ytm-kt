package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.uistrings.parseYoutubeDurationString
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveListItemRenderer(
    val playlistItemData: RendererPlaylistItemData?,
    val flexColumns: List<FlexColumn>?,
    val fixedColumns: List<FixedColumn>?,
    val thumbnail: ThumbnailRenderer?,
    val navigationEndpoint: NavigationEndpoint?,
    val menu: YoutubeiNextResponse.Menu?,
    val index: TextRuns?,
    val badges: List<Badge>?
) {
    @Serializable
    data class Badge(val musicInlineBadgeRenderer: MusicInlineBadgeRenderer?) {
        fun isExplicit(): Boolean = musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
    }
    @Serializable
    data class MusicInlineBadgeRenderer(val icon: YoutubeiNextResponse.MenuIcon?)

    fun toMediaItemAndPlaylistSetVideoId(hl: String): Pair<YtmMediaItem, String?>? {
        var video_id: String? = playlistItemData?.videoId ?: navigationEndpoint?.watchEndpoint?.videoId
        val browse_id: String? = navigationEndpoint?.browseEndpoint?.browseId
        var video_is_main: Boolean = true

        var title: String? = null
        var artists: MutableList<YtmArtist>? = null
        var playlist: YtmPlaylist? = null
        var duration: Long? = null
        var album: YtmPlaylist? = null

        if (video_id == null && browse_id != null) {
            val page_type: String? = navigationEndpoint!!.browseEndpoint!!.getPageType()
            when (
                page_type?.let { type ->
                    YtmMediaItem.Type.fromBrowseEndpointType(type)
                }
            ) {
                YtmMediaItem.Type.PLAYLIST -> {
                    video_is_main = false
                    playlist = YtmPlaylist(
                        YtmPlaylist.cleanId(browse_id),
                        type = YtmPlaylist.Type.fromBrowseEndpointType(page_type)
                    )
                }
                YtmMediaItem.Type.ARTIST -> {
                    video_is_main = false
                    artists = mutableListOf(YtmArtist(browse_id))
                }
                else -> {}
            }
        }

        if (flexColumns != null) {
            for (column in flexColumns.withIndex()) {
                val text = column.value.musicResponsiveListItemFlexColumnRenderer.text
                if (text.runs == null) {
                    continue
                }

                if (column.index == 0) {
                    title = text.first_text
                }

                for (run in text.runs!!) {
                    if (run.navigationEndpoint == null) {
                        continue
                    }

                    if (run.navigationEndpoint.watchEndpoint != null) {
                        if (video_id == null) {
                            video_id = run.navigationEndpoint.watchEndpoint.videoId!!
                        }
                        continue
                    }

                    val browse_endpoint: BrowseEndpoint = run.navigationEndpoint.browseEndpoint ?: continue
                    if (browse_endpoint.browseId != null && browse_endpoint.getMediaItemType() == YtmMediaItem.Type.ARTIST) {
                        if (artists?.any { it.id == browse_endpoint.browseId } == true) {
                            continue
                        }

                        if (artists == null) {
                            artists = mutableListOf()
                        }
                        artists!!.add(
                            YtmArtist(
                                browse_endpoint.browseId,
                                name = run.text
                            )
                        )
                    }
                }
            }
        }

        if (fixedColumns != null) {
            for (column in fixedColumns) {
                val text = column.musicResponsiveListItemFixedColumnRenderer.text.first_text
                val parsed = parseYoutubeDurationString(text, hl)
                if (parsed != null) {
                    duration = parsed
                    break
                }
            }
        }

        var item_data: YtmMediaItem
        val thumbnail_provider: ThumbnailProvider? = thumbnail?.toThumbnailProvider()

        if (video_id != null) {
            val first_thumbnail = thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
            val song_type: YtmSong.Type? = first_thumbnail?.let {
                if (it.height == it.width) YtmSong.Type.SONG else YtmSong.Type.VIDEO
            }

            item_data = YtmSong(
                YtmSong.cleanId(video_id),
                name = title,
                duration = duration,
                type = song_type,
                is_explicit = badges?.any { it.isExplicit() } == true,
                thumbnail_provider = thumbnail_provider
            )
        }
        else if (video_is_main) {
            return null
        }
        else {
            item_data =
                playlist?.copy(
                    total_duration = duration,
                    thumbnail_provider = thumbnail_provider,
                    name = title
                )
                ?: artists?.firstOrNull()?.let { artist ->
                    artist.copy(
                        thumbnail_provider = thumbnail_provider,
                        name = artist.name ?: title
                    )
                }
                ?: return null
        }

        // Handle songs with no artist (or 'Various artists')
        if (artists == null && (item_data is YtmSong || item_data is YtmPlaylist)) {
            if (flexColumns != null && flexColumns.size > 1) {
                val text = flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text
                if (text.runs != null) {
                    artists = mutableListOf(
                        YtmArtist(
                            YtmArtist.getForItemId(item_data),
                            name = text.first_text
                        )
                    )
                }
            }
        }

        for (item in menu?.menuRenderer?.items ?: emptyList()) {
            val browse_endpoint: BrowseEndpoint = (item.menuNavigationItemRenderer ?: continue).navigationEndpoint.browseEndpoint ?: continue
            if (browse_endpoint.browseId == null) {
                continue
            }

            when (browse_endpoint.getMediaItemType()) {
                YtmMediaItem.Type.ARTIST -> {
                    if (artists?.any { it.id == browse_endpoint.browseId } == true) {
                        continue
                    }

                    if (artists == null) {
                        artists = mutableListOf()
                    }
                    artists!!.add(YtmArtist(browse_endpoint.browseId))
                }
                YtmMediaItem.Type.PLAYLIST -> {
                    if (album == null) {
                        album = YtmPlaylist(YtmPlaylist.cleanId(browse_endpoint.browseId))
                    }
                }
                else -> {}
            }
        }

        if (item_data is YtmSong) {
            item_data = item_data.copy(
                artists = artists,
                album = album
            )
        }
        else if (item_data is YtmPlaylist) {
            item_data = item_data.copy(
                artists = artists
            )
        }

        return Pair(item_data, playlistItemData?.playlistSetVideoId)
    }
}

@Serializable
data class RendererPlaylistItemData(val videoId: String, val playlistSetVideoId: String?)
