package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.*
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtistLayout
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.uistrings.YoutubeUiString
import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation
import dev.toastbits.ytmkt.endpoint.ArtistRadioEndpoint
import dev.toastbits.ytmkt.endpoint.ArtistWithParamsRow
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import dev.toastbits.ytmkt.model.implementedOrNull

open class YTMArtistRadioEndpoint(override val api: YoutubeiApi): ArtistRadioEndpoint() {
    override suspend fun getArtistRadio(
        artist_id: String,
        continuation: String?
    ): Result<RadioData> = runCatching {
        val artist: YtmArtist = api.item_cache.loadArtist(
            api,
            artist_id,
            setOf(MediaItemCache.ArtistKey.LAYOUTS)
        )

        val layouts: List<YtmArtistLayout> = artist.layouts ?: throw NullPointerException("Artist $artist_id has no layouts")

        for (string_id in listOf(YoutubeUILocalisation.StringID.ARTIST_ROW_SONGS, YoutubeUILocalisation.StringID.ARTIST_ROW_VIDEOS)) {
            for (layout in layouts) {
                val title: YoutubeUiString = (layout.title as? YoutubeUiString) ?: continue
                if (title.getYoutubeStringId() != string_id) {
                    continue
                }

                val view_more: YoutubePage = layout.view_more ?: continue
                when (view_more) {
                    is MediaItemYoutubePage -> {
                        val songs_playlist_id: String = (view_more.browse_media_item as? YtmPlaylist)?.id ?: continue
                        val browse_params: String? = view_more.getBrowseParamsData()?.browse_params
                        
                        val playlist: YtmPlaylist
                        
                        if (browse_params == null) {
                            playlist = api.item_cache.loadPlaylist(
                                api,
                                songs_playlist_id,
                                setOf(MediaItemCache.PlaylistKey.ITEMS)
                            )
                        }
                        else {
                            playlist = api.LoadPlaylist.loadPlaylist(
                                songs_playlist_id,
                                browse_params = browse_params
                            ).getOrThrow()
                        }

                        if (playlist.items == null) {
                            continue
                        }

                        return@runCatching RadioData(playlist.items, null)
                    }
                    is ListPageBrowseIdYoutubePage -> {
                        val artist_endpoint: YTMArtistWithParamsEndpoint =
                            api.ArtistWithParams.implementedOrNull()
                            ?: continue
                        val rows: List<ArtistWithParamsRow> =
                            artist_endpoint.loadArtistWithParams(view_more.getBrowseParamsData()).getOrNull()
                            ?: continue

                        return@runCatching RadioData(rows.flatMap { it.items.filterIsInstance<YtmSong>() }, null)
                    }
                    is PlainYoutubePage -> continue
                }
            }
        }

        throw RuntimeException("Could not find items layout for $artist")
    }
}
