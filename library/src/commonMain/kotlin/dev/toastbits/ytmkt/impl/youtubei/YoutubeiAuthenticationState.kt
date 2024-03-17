package dev.toastbits.ytmkt.impl.youtubei

import dev.toastbits.ytmkt.model.ApiAuthenticationState
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.endpoint.GenericFeedViewMorePageEndpoint
import dev.toastbits.ytmkt.impl.youtubei.endpoint.*
import dev.toastbits.ytmkt.model.internal.YoutubeiBrowseResponse
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import kotlinx.serialization.json.put

/**
 * Contains user authentication information used by [YoutubeiApi]
 *
 * @param api The [YoutubeiApi] using this object.
 * @param headers Must contain the `authorization` and `cookie` headers.
 * @param own_channel_id The YouTube channel ID of the user. Used in some instances to mark items as being owned by the user.
 */
open class YoutubeiAuthenticationState(
    override val api: YoutubeiApi,
    headers: Headers,
    override val own_channel_id: String?
): ApiAuthenticationState(headers, INCLUDED_HEADERS) {
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

    companion object {
        val INCLUDED_HEADERS: List<String> = listOf("cookie", "authorization", "x-goog-authuser")
        val REQUIRED_HEADERS: List<String> = listOf("authorization", "cookie")
    }
}

open class YTMGenericFeedViewMorePageEndpoint(override val api: YoutubeiApi): GenericFeedViewMorePageEndpoint() {
    override suspend fun getGenericFeedViewMorePage(
        browse_id: String
    ): Result<List<YtmMediaItem>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody {
                put("browseId", browse_id)
            }
        }

        val data: YoutubeiBrowseResponse = response.body()

        val items: List<YtmMediaItem> =
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

class YoutubeChannelNotCreatedException(
    val headers: Headers,
    val channel_creation_token: String?
): RuntimeException()
