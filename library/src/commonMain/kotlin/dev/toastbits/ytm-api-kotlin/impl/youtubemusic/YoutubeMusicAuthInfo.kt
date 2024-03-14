package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.GenericFeedViewMorePageEndpoint
import dev.toastbits.ytmapi.endpoint.LikedAlbumsEndpoint
import dev.toastbits.ytmapi.endpoint.LikedArtistsEndpoint
import dev.toastbits.ytmapi.endpoint.LikedPlaylistsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.*
import dev.toastbits.ytmapi.model.internal.YoutubeiBrowseResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import kotlinx.serialization.json.put

class YoutubeChannelNotCreatedException(
    val headers: Headers,
    val channel_creation_token: String?
): RuntimeException()

class YoutubeMusicAuthInfo private constructor(
    override val api: YoutubeMusicApi,
    override val own_channel_id: String,
    headers: Headers
): YoutubeApi.UserAuthState(headers) {
    companion object {
        val REQUIRED_HEADERS: List<String> = listOf("authorization", "cookie")

        fun create(api: YoutubeMusicApi, own_channel_id: String, headers: Headers): YoutubeMusicAuthInfo {
            return YoutubeMusicAuthInfo(api, own_channel_id, headers)
        }
    }

    override val AccountPlaylists = YTMAccountPlaylistsEndpoint(this)
    override val CreateAccountPlaylist = YTMCreateAccountPlaylistEndpoint(this)
    override val DeleteAccountPlaylist = YTMDeleteAccountPlaylistEndpoint(this)
    override val SubscribedToArtist = YTMSubscribedToArtistEndpoint(this)
    override val SetSubscribedToArtist = YTMSetSubscribedToArtistEndpoint(this)
    override val SongLiked = YTMSongLikedEndpoint(this)
    override val SetSongLiked = YTMSetSongLikedEndpoint(this)
    override val MarkSongAsWatched = YTMMarkSongAsWatchedEndpoint(this)
    override val AccountPlaylistEditor = YTMAccountPlaylistEditorEndpoint(this)
    override val AccountPlaylistAddSongs = YTMAccountPlaylistAddSongsEndpoint(this)
    override val LikedAlbums = YTMLikedAlbumsEndpoint(this)
    override val LikedArtists = YTMLikedArtistsEndpoint(this)
    override val LikedPlaylists = YTMLikedPlaylistsEndpoint(this)
}

class YTMGenericFeedViewMorePageEndpoint(override val api: YoutubeApi): GenericFeedViewMorePageEndpoint() {
    override suspend fun getGenericFeedViewMorePage(
        browse_id: String
    ): Result<List<MediaItem>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody {
                put("browseId", browse_id)
            }
        }

        val data: YoutubeiBrowseResponse = response.body()

        val items: List<MediaItem> =
            data.contents!!
                .singleColumnBrowseResultsRenderer!!
                .tabs
                .first()
                .tabRenderer
                .content!!
                .sectionListRenderer!!
                .contents!!
                .first()
                .getMediaItems(hl, api)

        return@runCatching items
    }
}
