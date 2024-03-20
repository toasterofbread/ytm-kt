@file:Suppress("MemberVisibilityCanBePrivate")

package dev.toastbits.ytmkt.radio

import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.internal.BrowseEndpoint
import dev.toastbits.ytmkt.model.internal.TextRuns
import dev.toastbits.ytmkt.model.internal.WatchEndpoint
import dev.toastbits.ytmkt.model.internal.MusicThumbnailRenderer
import dev.toastbits.ytmkt.model.internal.MusicResponsiveListItemRenderer
import dev.toastbits.ytmkt.model.internal.TextRun
import dev.toastbits.ytmkt.model.internal.NavigationEndpoint
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeiNextResponse(
    val contents: Contents
) {
    @Serializable
    class Contents(val singleColumnMusicWatchNextResultsRenderer: SingleColumnMusicWatchNextResultsRenderer)
    @Serializable
    class SingleColumnMusicWatchNextResultsRenderer(val tabbedRenderer: TabbedRenderer)
    @Serializable
    class TabbedRenderer(val watchNextTabbedResultsRenderer: WatchNextTabbedResultsRenderer)
    @Serializable
    class WatchNextTabbedResultsRenderer(val tabs: List<Tab>)
    @Serializable
    class Tab(val tabRenderer: TabRenderer)
    @Serializable
    class TabRenderer(val content: Content?, val endpoint: TabRendererEndpoint?)
    @Serializable
    class TabRendererEndpoint(val browseEndpoint: BrowseEndpoint)
    @Serializable
    class Content(val musicQueueRenderer: MusicQueueRenderer)
    @Serializable
    class MusicQueueRenderer(val content: MusicQueueRendererContent?, val subHeaderChipCloud: SubHeaderChipCloud?)

    @Serializable
    class SubHeaderChipCloud(val chipCloudRenderer: ChipCloudRenderer)
    @Serializable
    class ChipCloudRenderer(val chips: List<Chip>)
    @Serializable
    class Chip(val chipCloudChipRenderer: ChipCloudChipRenderer) {
        fun getPlaylistId(): String = chipCloudChipRenderer.navigationEndpoint.queueUpdateCommand.fetchContentsCommand.watchEndpoint.playlistId!!
    }
    @Serializable
    class ChipCloudChipRenderer(val navigationEndpoint: ChipNavigationEndpoint)
    @Serializable
    class ChipNavigationEndpoint(val queueUpdateCommand: QueueUpdateCommand)
    @Serializable
    class QueueUpdateCommand(val fetchContentsCommand: FetchContentsCommand)
    @Serializable
    class FetchContentsCommand(val watchEndpoint: WatchEndpoint)

    @Serializable
    class MusicQueueRendererContent(val playlistPanelRenderer: PlaylistPanelRenderer)
    @Serializable
    class PlaylistPanelRenderer(val contents: List<ResponseRadioItem>, val continuations: List<Continuation>? = null)
    @Serializable
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

    @Serializable
    class PlaylistPanelVideoWrapperRenderer(
        val primaryRenderer: ResponseRadioItem
    )

    @Serializable
    class PlaylistPanelVideoRenderer(
        val videoId: String,
        val title: TextRuns,
        val longBylineText: TextRuns,
        val menu: Menu,
        val thumbnail: MusicThumbnailRenderer.RendererThumbnail,
        val badges: List<MusicResponsiveListItemRenderer.Badge>?
    ) {
        suspend fun getArtists(api: YtmApi): Result<List<YtmArtist>?> = runCatching {
            // Get artist IDs directly
            val artists: List<YtmArtist>? = (longBylineText.runs.orEmpty() + title.runs.orEmpty())
                .mapNotNull { run ->
                    val browse_id: String = run.navigationEndpoint?.browseEndpoint?.browseId
                        ?: return@mapNotNull null

                    val page_type = run.browse_endpoint_type?.let { type ->
                        YtmMediaItem.Type.fromBrowseEndpointType(type)
                    }
                    if (page_type != YtmMediaItem.Type.ARTIST) {
                        return@mapNotNull null
                    }

                    return@mapNotNull YtmArtist(
                        id = browse_id,
                        name = run.text
                    )
                }

            if (!artists.isNullOrEmpty()) {
                return@runCatching artists
            }

            val menu_artist: String? = menu.menuRenderer.getArtist()?.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint?.browseId
            if (menu_artist != null) {
                return@runCatching listOf(YtmArtist(menu_artist))
            }

            // Get artist from album
            for (run in longBylineText.runs!!) {
                if (run.navigationEndpoint?.browseEndpoint?.getPageType() != "MUSIC_PAGE_TYPE_ALBUM") {
                    continue
                }

                val playlist_id: String = run.navigationEndpoint.browseEndpoint.browseId ?: continue
                val playlist: YtmPlaylist = api.item_cache.loadPlaylist(
                    api,
                    playlist_id,
                    setOf(MediaItemCache.PlaylistKey.ARTIST_ID)
                )

                if (playlist.artists != null) {
                    return@runCatching playlist.artists
                }
            }

            // Get title-only artist (Resolves to 'Various artists' when viewed on YouTube)
            val artist_title: TextRun? = longBylineText.runs?.firstOrNull { it.navigationEndpoint == null }
            if (artist_title != null) {
                return@runCatching listOf(
                    YtmArtist(
                        id = "",
                        name = artist_title.text
                    )
                )
            }

            return@runCatching null
        }
    }
    @Serializable
    data class Menu(val menuRenderer: MenuRenderer)
    @Serializable
    data class MenuRenderer(val items: List<MenuItem>) {
        fun getArtist(): MenuItem? =
            items.firstOrNull {
                it.menuNavigationItemRenderer?.icon?.iconType == "ARTIST"
            }
    }
    @Serializable
    data class MenuItem(val menuNavigationItemRenderer: MenuNavigationItemRenderer?)
    @Serializable
    data class MenuNavigationItemRenderer(val icon: MenuIcon, val navigationEndpoint: NavigationEndpoint)
    @Serializable
    data class MenuIcon(val iconType: String)
    @Serializable
    data class Continuation(val nextContinuationData: ContinuationData?, val nextRadioContinuationData: ContinuationData?) {
        val data: ContinuationData? get() = nextContinuationData ?: nextRadioContinuationData
    }
    @Serializable
    data class ContinuationData(val continuation: String)
}

@Serializable
data class YoutubeiNextContinuationResponse(
    val continuationContents: Contents
) {
    @Serializable
    data class Contents(val playlistPanelContinuation: YoutubeiNextResponse.PlaylistPanelRenderer)
}
