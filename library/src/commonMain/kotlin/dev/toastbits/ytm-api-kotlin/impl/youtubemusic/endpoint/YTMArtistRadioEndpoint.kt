package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.*
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.ArtistLayout
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.uistrings.YoutubeUiString
import dev.toastbits.ytmapi.uistrings.YoutubeUILocalisation
import dev.toastbits.ytmapi.endpoint.ArtistRadioEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.itemcache.MediaItemCache

class YTMArtistRadioEndpoint(override val api: YoutubeMusicApi): ArtistRadioEndpoint() {
    override suspend fun getArtistRadio(
        artist_id: String,
        continuation: String?
    ): Result<RadioData> = runCatching {
        val artist: Artist = api.item_cache.loadArtist(
            api,
            artist_id,
            setOf(MediaItemCache.ArtistKey.LAYOUTS)
        )

        val layouts: List<ArtistLayout> = artist.layouts ?: throw NullPointerException("Artist $artist_id has no layouts")

        for (string_id in listOf(YoutubeUILocalisation.StringID.ARTIST_ROW_SONGS, YoutubeUILocalisation.StringID.ARTIST_ROW_VIDEOS)) {
            for (layout in layouts) {
                val title: YoutubeUiString = (layout.title as? YoutubeUiString) ?: continue
                if (title.getYoutubeStringId() != string_id) {
                    continue
                }

                val view_more: YoutubePage = layout.view_more ?: continue
                when (view_more) {
                    is MediaItemYoutubePage -> {
                        val songs_playlist_id: String = (view_more.browse_media_item as? Playlist)?.id ?: continue
                        val playlist: Playlist = api.item_cache.loadPlaylist(
                            api,
                            songs_playlist_id,
                            setOf(MediaItemCache.PlaylistKey.ITEMS)
                        )

                        if (playlist.items == null) {
                            continue
                        }

                        return@runCatching RadioData(playlist.items, null)
                    }
                    is ListPageBrowseIdYoutubePage -> {
                        val artist_endpoint = api.ArtistWithParams.implementedOrNull() ?: continue
                        val rows = artist_endpoint.loadArtistWithParams(view_more.getBrowseParamsData()).getOrNull() ?: continue

                        return@runCatching RadioData(rows.flatMap { it.items.filterIsInstance<Song>() }, null)
                    }
                    is PlainYoutubePage -> continue
                }
            }
        }

        throw RuntimeException("Could not find items layout for $artist")
    }
}
