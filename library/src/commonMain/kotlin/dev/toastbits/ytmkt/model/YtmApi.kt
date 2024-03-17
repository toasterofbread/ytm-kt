package dev.toastbits.ytmkt.model

import dev.toastbits.ytmkt.endpoint.*
import dev.toastbits.ytmkt.formats.VideoFormatsEndpoint
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

/**
 * Contains all non-authenticated YouTube endpoints.
 *
 * Deriving classes must provide implememtations for each endpoint.
 * [dev.toastbits.ytmkt.impl.unimplemented.UnimplementedYtmApi] may be used as a base for partial implementations of this interface.
 * See endpoint class definitions for usage documentation.
 *
 * @property client Ktor [HttpClient] to be used by endpoints.
 * @property item_cache Provides cached [YtmMediaItem]s to endpoints.
 * @property user_auth_state The user authentication information to be used by endpoints.
 */
interface YtmApi {
    val client: HttpClient
    val item_cache: MediaItemCache
    val user_auth_state: ApiAuthenticationState?

    fun HttpRequestBuilder.endpointPath(path: String)

    fun HttpRequestBuilder.postWithBody(
        base: JsonObject? = null,
        buildPostBody: (JsonObjectBuilder.() -> Unit)? = null
    )

    fun HttpRequestBuilder.addUnauthenticatedApiHeaders(include: List<String>? = null)

    fun HttpRequestBuilder.addAuthenticatedApiHeaders(include: List<String>? = null) {
        addUnauthenticatedApiHeaders(include)
        user_auth_state?.addHeadersToRequest(this, include)
    }

    // -- User auth ---
    val YoutubeChannelCreationForm: YoutubeChannelCreationFormEndpoint
    val CreateYoutubeChannel: CreateYoutubeChannelEndpoint

    // --- Item loading ---
    val LoadSong: LoadSongEndpoint
    val LoadArtist: LoadArtistEndpoint
    val LoadPlaylist: LoadPlaylistEndpoint

    // --- Video formats ---
    val VideoFormats: VideoFormatsEndpoint

    // --- Feed ---
    val SongFeed: SongFeedEndpoint
    val GenericFeedViewMorePage: GenericFeedViewMorePageEndpoint
    val SongRadio: SongRadioEndpoint

    // --- Artists ---
    val ArtistWithParams: ArtistWithParamsEndpoint
    val ArtistRadio: ArtistRadioEndpoint
    val ArtistShuffle: ArtistShuffleEndpoint

    // --- Playlists ---
    val PlaylistContinuation: PlaylistContinuationEndpoint

    // --- Search ---
    val Search: SearchEndpoint
    val SearchSuggestions: SearchSuggestionsEndpoint

    // --- Radio builder ---
    val RadioBuilder: RadioBuilderEndpoint

    // --- Song content ---
    val SongRelatedContent: SongRelatedContentEndpoint
    val SongLyrics: SongLyricsEndpoint
}
