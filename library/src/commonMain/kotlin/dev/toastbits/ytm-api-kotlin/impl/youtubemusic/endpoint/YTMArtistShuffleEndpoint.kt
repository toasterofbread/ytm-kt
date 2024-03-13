package dev.toastbits.ytmapi.impl.youtubemusic.endpoint

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.artist.ArtistLayout
import dev.toastbits.ytmapi.model.external.mediaitem.layout.LambdaViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.ListPageBrowseIdViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.PlainViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.layout.ViewMore
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylist
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import com.toasterofbread.spmp.resources.uilocalisation.YoutubeLocalisedString
import com.toasterofbread.spmp.resources.uilocalisation.YoutubeUILocalisation
import dev.toastbits.ytmapi.endpoint.ArtistRadioEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import java.io.IOException

class YTMArtistRadioEndpoint(override val api: YoutubeMusicApi): ArtistRadioEndpoint() {
    override suspend fun getArtistRadio(
        artist: Artist,
        continuation: String?
    ): Result<RadioData> = runCatching {
        var layouts: List<ArtistLayout>? = artist.Layouts.get(api.database)
        if (layouts == null) {
            artist.loadData(api.context, populate_data = false).onFailure {
                return@runCatching Result.failure(IOException(it))
            }
            layouts = artist.Layouts.get(api.database)

            if (layouts == null) {
                return@runCatching Result.failure(NullPointerException("$artist layouts is null"))
            }
        }

        for (string_id in listOf(YoutubeUILocalisation.StringID.ARTIST_ROW_SONGS, YoutubeUILocalisation.StringID.ARTIST_ROW_VIDEOS)) {
            for (layout in layouts) {
                val title: YoutubeLocalisedString = (layout.Title.get(api.database) as? YoutubeLocalisedString) ?: continue
                if (title.getYoutubeStringId() != string_id) {
                    continue
                }

                val view_more: ViewMore = layout.ViewMore.get(api.database) ?: continue
                when (view_more) {
                    is MediaItemViewMore -> {
                        val songs_playlist: RemotePlaylist = (view_more.browse_media_item as? RemotePlaylist) ?: continue
                        val items = songs_playlist.loadData(api.context).getOrNull()?.items ?: continue

                        return@runCatching Result.success(
                            RadioData(items, null)
                        )
                    }
                    is ListPageBrowseIdViewMore -> {
                        val artist_endpoint = api.ArtistWithParams.implementedOrNull() ?: continue
                        val rows = artist_endpoint.loadArtistWithParams(view_more.getBrowseParamsData(title, api.context)).getOrNull() ?: continue

                        return@runCatching Result.success(
                            RadioData(rows.flatMap { it.items.filterIsInstance<SongData>() }, null)
                        )
                    }
                    is PlainViewMore, is LambdaViewMore -> continue
                }
            }
        }

        return@runCatching Result.failure(RuntimeException("Could not find items layout for $artist"))
    }
}
