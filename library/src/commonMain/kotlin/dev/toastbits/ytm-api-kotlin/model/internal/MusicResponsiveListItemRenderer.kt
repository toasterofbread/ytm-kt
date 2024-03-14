package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.ThumbnailProvider
import dev.toastbits.ytmapi.uistrings.parseYoutubeDurationString
import dev.toastbits.ytmapi.radio.YoutubeiNextResponse

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
    data class Badge(val musicInlineBadgeRenderer: MusicInlineBadgeRenderer?) {
        fun isExplicit(): Boolean = musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
    }
    data class MusicInlineBadgeRenderer(val icon: YoutubeiNextResponse.MenuIcon?)

    fun toMediaItemAndPlaylistSetVideoId(hl: String): Pair<MediaItem, String?>? {
        var video_id: String? = playlistItemData?.videoId ?: navigationEndpoint?.watchEndpoint?.videoId
        val browse_id: String? = navigationEndpoint?.browseEndpoint?.browseId
        var video_is_main: Boolean = true

        var title: String? = null
        var artist: Artist? = null
        var playlist: Playlist? = null
        var duration: Long? = null
        var album: Playlist? = null

        if (video_id == null && browse_id != null) {
            val page_type: String? = navigationEndpoint!!.browseEndpoint!!.getPageType()
            when (
                page_type?.let { type ->
                    MediaItem.Type.fromBrowseEndpointType(type)
                }
            ) {
                MediaItem.Type.PLAYLIST -> {
                    video_is_main = false
                    playlist = Playlist(
                        browse_id,
                        type = Playlist.Type.fromBrowseEndpointType(page_type)
                    )
                }
                MediaItem.Type.ARTIST -> {
                    video_is_main = false
                    artist = Artist(browse_id)
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
                    if (artist == null && browse_endpoint.browseId != null && browse_endpoint.getMediaItemType() == MediaItem.Type.ARTIST) {
                        artist = Artist(
                            browse_endpoint.browseId,
                            name = run.text
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

        var item_data: MediaItem
        val thumbnail_provider: ThumbnailProvider? = thumbnail?.toThumbnailProvider()

        if (video_id != null) {
            val first_thumbnail = thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
            val song_type: Song.Type? = first_thumbnail?.let {
                if (it.height == it.width) Song.Type.SONG else Song.Type.VIDEO
            }

            item_data = Song(
                video_id,
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
                    thumbnail_provider = thumbnail_provider
                )
                ?: artist?.copy(
                    thumbnail_provider = thumbnail_provider
                ) 
                ?: return null
        }

        // Handle songs with no artist (or 'Various artists')
        if (artist == null && (item_data is Song || item_data is Playlist)) {
            if (flexColumns != null && flexColumns.size > 1) {
                val text = flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text
                if (text.runs != null) {
                    artist = Artist(
                        Artist.getForItemId(item_data),
                        name = text.first_text
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
                MediaItem.Type.ARTIST -> {
                    if (artist == null) {
                        artist = Artist(browse_endpoint.browseId)
                    }
                }
                MediaItem.Type.PLAYLIST -> {
                    if (album == null) {
                        album = Playlist(browse_endpoint.browseId)
                    }
                }
                else -> {}
            }
        }

        if (item_data is Song) {
            item_data = item_data.copy(
                artist = artist,
                album = album
            )
        }
        else if (item_data is Playlist) {
            item_data = item_data.copy(
                artist = artist
            )
        }

        return Pair(item_data, playlistItemData?.playlistSetVideoId)
    }
}

data class RendererPlaylistItemData(val videoId: String, val playlistSetVideoId: String?)
