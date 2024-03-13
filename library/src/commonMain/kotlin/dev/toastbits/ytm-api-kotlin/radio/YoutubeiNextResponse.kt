@file:Suppress("MemberVisibilityCanBePrivate")

package dev.toastbits.ytmapi.radio

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.db.loadMediaItemValue
import dev.toastbits.ytmapi.model.external.mediaitem.enums.MediaItemType
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.*

data class YoutubeiNextResponse(
    val contents: Contents
) {
    class Contents(val singleColumnMusicWatchNextResultsRenderer: SingleColumnMusicWatchNextResultsRenderer)
    class SingleColumnMusicWatchNextResultsRenderer(val tabbedRenderer: TabbedRenderer)
    class TabbedRenderer(val watchNextTabbedResultsRenderer: WatchNextTabbedResultsRenderer)
    class WatchNextTabbedResultsRenderer(val tabs: List<Tab>)
    class Tab(val tabRenderer: TabRenderer)
    class TabRenderer(val content: Content?, val endpoint: TabRendererEndpoint?)
    class TabRendererEndpoint(val browseEndpoint: BrowseEndpoint)
    class Content(val musicQueueRenderer: MusicQueueRenderer)
    class MusicQueueRenderer(val content: MusicQueueRendererContent?, val subHeaderChipCloud: SubHeaderChipCloud?)

    class SubHeaderChipCloud(val chipCloudRenderer: ChipCloudRenderer)
    class ChipCloudRenderer(val chips: List<Chip>)
    class Chip(val chipCloudChipRenderer: ChipCloudChipRenderer) {
        fun getPlaylistId(): String = chipCloudChipRenderer.navigationEndpoint.queueUpdateCommand.fetchContentsCommand.watchEndpoint.playlistId!!
    }
    class ChipCloudChipRenderer(val navigationEndpoint: ChipNavigationEndpoint)
    class ChipNavigationEndpoint(val queueUpdateCommand: QueueUpdateCommand)
    class QueueUpdateCommand(val fetchContentsCommand: FetchContentsCommand)
    class FetchContentsCommand(val watchEndpoint: WatchEndpoint)

    class MusicQueueRendererContent(val playlistPanelRenderer: PlaylistPanelRenderer)
    class PlaylistPanelRenderer(val contents: List<ResponseRadioItem>, val continuations: List<Continuation>? = null)
    data class ResponseRadioItem(
        val playlistPanelVideoRenderer: PlaylistPanelVideoRenderer?,
        val playlistPanelVideoWrapperRenderer: PlaylistPanelVideoWrapperRenderer?
    ) {
        fun getRenderer(): PlaylistPanelVideoRenderer {
            if (playlistPanelVideoRenderer != null) {
                return playlistPanelVideoRenderer
            }

            if (playlistPanelVideoWrapperRenderer == null) {
                throw NotImplementedError("Unimplemented renderer object in ResponseRadioItem")
            }

            return playlistPanelVideoWrapperRenderer.primaryRenderer.getRenderer()
        }
    }

    class PlaylistPanelVideoWrapperRenderer(
        val primaryRenderer: ResponseRadioItem
    )

    class PlaylistPanelVideoRenderer(
        val videoId: String,
        val title: TextRuns,
        val longBylineText: TextRuns,
        val menu: Menu,
        val thumbnail: MusicThumbnailRenderer.Thumbnail,
        val badges: List<MusicResponsiveListItemRenderer.Badge>?
    ) {
        suspend fun getArtist(): Result<Artist?> = runCatching {
            // Get artist ID directly
            for (run in longBylineText.runs!! + title.runs!!) {
                val page_type = run.browse_endpoint_type?.let { type ->
                    MediaItemType.fromBrowseEndpointType(type)
                }
                if (page_type != MediaItemType.ARTIST) {
                    continue
                }

                val browse_id: String = run.navigationEndpoint?.browseEndpoint?.browseId ?: continue
                return@runCatching Artist(
                    id = browse_id,
                    title = run.text
                )
            }

            val menu_artist: String? = menu.menuRenderer.getArtist()?.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint?.browseId
            if (menu_artist != null) {
                return@runCatching Artist(menu_artist)
            }

            // Get artist from album
            for (run in longBylineText.runs!!) {
                if (run.navigationEndpoint?.browseEndpoint?.getPageType() != "MUSIC_PAGE_TYPE_ALBUM") {
                    continue
                }

                val playlist_id: String = run.navigationEndpoint.browseEndpoint.browseId ?: continue

                var artist_id: String? = item_cache.getPlaylistArtist(playlist_id)

                if (artist_id == null) {
                    val playlist: Playlist? = api.LoadPlaylist.implementedOrNull().loadPlaylist()
                    artist_id = playlist?.artist_id

                    val artist = artist_load_result?.fold(
                        { it },
                        { return Result.failure(it) }
                    )
                }

                if (artist_id != null) {
                    return@runCatching Artist(artist_id)
                }
            }

            // Get title-only artist (Resolves to 'Various artists' when viewed on YouTube)
            val artist_title: TextRun? = longBylineText.runs?.firstOrNull { it.navigationEndpoint == null }
            if (artist_title != null) {
                return@runCatching Artist(
                    id = "",
                    title = artist_title.text
                )
            }

            return@runCatching null
        }
    }
    data class Menu(val menuRenderer: MenuRenderer)
    data class MenuRenderer(val items: List<MenuItem>) {
        fun getArtist(): MenuItem? =
            items.firstOrNull {
                it.menuNavigationItemRenderer?.icon?.iconType == "ARTIST"
            }
    }
    data class MenuItem(val menuNavigationItemRenderer: MenuNavigationItemRenderer?)
    data class MenuNavigationItemRenderer(val icon: MenuIcon, val navigationEndpoint: NavigationEndpoint)
    data class MenuIcon(val iconType: String)
    data class Continuation(val nextContinuationData: ContinuationData?, val nextRadioContinuationData: ContinuationData?) {
        val data: ContinuationData? get() = nextContinuationData ?: nextRadioContinuationData
    }
    data class ContinuationData(val continuation: String)
}

data class YoutubeiNextContinuationResponse(
    val continuationContents: Contents
) {
    data class Contents(val playlistPanelContinuation: YoutubeiNextResponse.PlaylistPanelRenderer)
}
