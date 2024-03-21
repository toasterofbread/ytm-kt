package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import kotlinx.serialization.Serializable

@Serializable
data class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint,
    val title: TextRuns,
    val subtitle: TextRuns?,
    val thumbnailRenderer: ThumbnailRenderer,
    val menu: YoutubeiNextResponse.Menu?,
    val subtitleBadges: List<MusicResponsiveListItemRenderer.Badge>?
) {
    private fun getArtists(host_item: YtmMediaItem, api: YtmApi): List<YtmArtist>? {
        val artists: List<YtmArtist>? = subtitle?.runs?.mapNotNull { run ->
            val browse_endpoint: BrowseEndpoint? = run.navigationEndpoint?.browseEndpoint
            if (browse_endpoint?.browseId == null || browse_endpoint.getMediaItemType() != YtmMediaItem.Type.ARTIST) {
                return@mapNotNull null
            }

            return@mapNotNull YtmArtist(
                browse_endpoint.browseId,
                name = run.text
            )
        }

        if (!artists.isNullOrEmpty()) {
            return artists
        }

        if (host_item is YtmSong) {
            val song_type: YtmSong.Type? = api.item_cache.getSong(
                host_item.id,
                setOf(MediaItemCache.SongKey.TYPE)
            )?.type

            val index: Int = if (song_type == YtmSong.Type.VIDEO) 0 else 1
            subtitle?.runs?.getOrNull(index)?.also {
                return listOf(
                    YtmArtist(YtmArtist.getForItemId(host_item)).copy(
                        name = it.text
                    )
                )
            }
        }

        return null
    }

    fun toMediaItem(api: YtmApi): YtmMediaItem? {
        // Video
        if (navigationEndpoint.watchEndpoint?.videoId != null) {
            var album: YtmPlaylist? = null
            for (item in menu?.menuRenderer?.items ?: emptyList()) {
                val browse_endpoint: BrowseEndpoint = item.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint ?: continue
                if (browse_endpoint.browseId != null && browse_endpoint.getMediaItemType() == YtmMediaItem.Type.PLAYLIST) {
                    album = YtmPlaylist(YtmPlaylist.cleanId(browse_endpoint.browseId))
                    break
                }
            }

            val first_thumbnail = thumbnailRenderer.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
            val song_id: String = YtmSong.cleanId(navigationEndpoint.watchEndpoint.videoId)

            return YtmSong(
                id = song_id,
                type =
                    if (first_thumbnail?.height == first_thumbnail?.width) YtmSong.Type.SONG
                    else YtmSong.Type.VIDEO,
                name = this@MusicTwoRowItemRenderer.title.first_text,
                thumbnail_provider = thumbnailRenderer.toThumbnailProvider(),
                artists = getArtists(YtmSong(song_id), api),
                is_explicit = subtitleBadges?.any { it.isExplicit() } == true,
                album = album
            )
        }

        val item: YtmMediaItem

        if (navigationEndpoint.watchPlaylistEndpoint != null) {
            item = YtmPlaylist(
                id = YtmPlaylist.cleanId(navigationEndpoint.watchPlaylistEndpoint.playlistId),
                type = YtmPlaylist.Type.RADIO,
                name = title.first_text,
                thumbnail_provider = thumbnailRenderer.toThumbnailProvider()
            )
        }
        else {
            // Playlist or artist
            val browse_id: String = navigationEndpoint.browseEndpoint?.browseId ?: return null
            val page_type: String = navigationEndpoint.browseEndpoint.getPageType() ?: return null

            val title: String = title.first_text
            val thumbnail_provider: ThumbnailProvider? = thumbnailRenderer.toThumbnailProvider()

            item = when (YtmMediaItem.Type.fromBrowseEndpointType(page_type)) {
                YtmMediaItem.Type.SONG -> {
                    val song_id: String = YtmSong.cleanId(browse_id)
                    YtmSong(
                        song_id,
                        name = title,
                        thumbnail_provider = thumbnail_provider,
                        artists = getArtists(YtmSong(song_id), api)
                    )
                }
                YtmMediaItem.Type.ARTIST ->
                    YtmArtist(
                        browse_id,
                        name = title,
                        thumbnail_provider = thumbnail_provider
                    )
                YtmMediaItem.Type.PLAYLIST ->{
                    val playlist_id: String = YtmPlaylist.cleanId(browse_id)
                    YtmPlaylist(
                        playlist_id,
                        type = YtmPlaylist.Type.fromBrowseEndpointType(page_type),
                        artists = getArtists(YtmPlaylist(playlist_id), api),
                        name = title,
                        thumbnail_provider = thumbnail_provider
                    )
                }
            }
        }

        return item
    }
}
