package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.endpoint.RadioBuilderModifier
import dev.toastbits.ytmkt.endpoint.SongRadioEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.radio.YoutubeiNextContinuationResponse
import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.put

private const val RADIO_ID_PREFIX = "RDAMVM"
private const val MODIFIED_RADIO_ID_PREFIX = "RDAT"

open class YTMSongRadioEndpoint(override val api: YoutubeiApi): SongRadioEndpoint() {
    override suspend fun getSongRadio(
        song_id: String,
        continuation: String?,
        filters: List<RadioBuilderModifier>
    ): Result<RadioData> = runCatching {
        for (filter in filters) {
            if (filter !is RadioBuilderModifier.Internal) {
                continue
            }

            when (filter) {
                RadioBuilderModifier.Internal.ARTIST -> {
                    val song: YtmSong = api.item_cache.loadSong(
                        api,
                        song_id,
                        setOf(MediaItemCache.SongKey.ARTIST_ID)
                    )

                    val artist: YtmArtist = song.artists?.firstOrNull()
                        ?: throw NullPointerException("Song $song_id has no artist")

                    val radio = api.ArtistRadio.getArtistRadio(artist.id, null).getOrThrow()
                    return@runCatching RadioData(radio.items, radio.continuation, null)
                }
            }
        }

        val response: HttpResponse = api.client.request {
            endpointPath("next")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("enablePersistentPlaylistPanel", true)
                put("tunerSettingValue", "AUTOMIX_SETTING_NORMAL")
                put("playlistId", videoIdToRadio(song_id, filters.filter { it !is RadioBuilderModifier.Internal }))
                put("isAudioOnly", true)
                putJsonObject("watchEndpointMusicSupportedConfigs") {
                    putJsonObject("watchEndpointMusicConfig") {
                        put("hasPersistentPlaylistPanel", true)
                        put("musicVideoType", "MUSIC_VIDEO_TYPE_ATV")
                    }
                }
                if (continuation != null) {
                    put("continuation", continuation)
                }
            }
        }

        val radio: YoutubeiNextResponse.PlaylistPanelRenderer?
        val out_filters: List<List<RadioBuilderModifier>>?

        if (continuation == null) {
            val data: YoutubeiNextResponse = response.body()

            val renderer: YoutubeiNextResponse.MusicQueueRenderer = data
                .contents
                .singleColumnMusicWatchNextResultsRenderer
                .tabbedRenderer
                .watchNextTabbedResultsRenderer
                .tabs
                .first()
                .tabRenderer
                .content!!
                .musicQueueRenderer

            radio = renderer.content?.playlistPanelRenderer
            out_filters = renderer.subHeaderChipCloud?.chipCloudRenderer?.chips?.mapNotNull { chip ->
                radioToFilters(chip.getPlaylistId(), song_id)
            }
        }
        else {
            val data: YoutubeiNextContinuationResponse = response.body()
            radio = data.continuationContents.playlistPanelContinuation
            out_filters = null
        }

        return@runCatching RadioData(
            radio?.contents?.map { item ->
                val renderer = item.getRenderer()

                return@map YtmSong(
                    YtmSong.cleanId(renderer.videoId),
                    name = renderer.title.first_text,
                    artists = renderer.getArtists(api).getOrThrow()
                )
            } ?: emptyList(),
            radio?.continuations?.firstOrNull()?.data?.continuation,
            out_filters
        )
    }
}

private fun radioToFilters(radio: String, song_id: String): List<RadioBuilderModifier>? {
    if (!radio.startsWith(MODIFIED_RADIO_ID_PREFIX)) {
        return null
    }

    val ret: MutableList<RadioBuilderModifier> = mutableListOf()
    val modifier_string = radio.substring(MODIFIED_RADIO_ID_PREFIX.length, radio.length - song_id.length)

    var c = 0
    while (c + 1 < modifier_string.length) {
        val modifier = RadioBuilderModifier.fromString(modifier_string.substring(c++, ++c))
        if (modifier != null) {
            ret.add(modifier)
        }
    }

    if (ret.isEmpty()) {
        return null
    }

    return ret
}

private fun videoIdToRadio(video_id: String, filters: List<RadioBuilderModifier>): String {
    if (filters.isEmpty()) {
        return RADIO_ID_PREFIX + video_id
    }

    val ret = StringBuilder(MODIFIED_RADIO_ID_PREFIX)
    for (filter in filters) {
        filter.string?.also { ret.append(it) }
    }
    ret.append('v')
    ret.append(video_id)
    return ret.toString()
}
