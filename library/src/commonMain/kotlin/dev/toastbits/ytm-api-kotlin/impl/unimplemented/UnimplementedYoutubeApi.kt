package dev.toastbits.ytmapi.impl.unimplemented

import dev.toastbits.ytmapi.model.external.mediaitem.*
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemLayout
import dev.toastbits.ytmapi.RadioBuilderArtist
import dev.toastbits.ytmapi.RadioBuilderEndpoint
import dev.toastbits.ytmapi.RadioBuilderModifier
import dev.toastbits.ytmapi.SongRelatedContentEndpoint
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.external.YoutubeVideoFormat
import dev.toastbits.ytmapi.model.external.YoutubePage
import dev.toastbits.ytmapi.model.external.Thumbnail
import dev.toastbits.ytmapi.endpoint.*
import dev.toastbits.ytmapi.formats.VideoFormatsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.RelatedGroup
import dev.toastbits.ytmapi.itemcache.MediaItemCache
import dev.toastbits.ytmapi.radio.RadioContinuation
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.Headers
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonObject

open class UnimplementedYoutubeApi(
    override val data_language: String = "",
    override val ui_language: String = "",
    override val item_cache: MediaItemCache = MediaItemCache(),
    override val client: HttpClient = HttpClient(CIO)
): YoutubeApi {
    override suspend fun init() {}

    override suspend fun YoutubeApi.PostBodyContext.getContextPostBody(): JsonObject {
        throw NotImplementedError()
    }

    override fun HttpRequestBuilder.endpointPath(path: String): HttpRequestBuilder {
        throw NotImplementedError()
    }

    override suspend fun HttpRequestBuilder.addAuthlessApiHeaders(include: List<String>?): HttpRequestBuilder {
        throw NotImplementedError()
    }

    override suspend fun HttpRequestBuilder.postWithBody(
        context: YoutubeApi.PostBodyContext,
        buildPostBody: (JsonObjectBuilder.() -> Unit)?
    ): HttpRequestBuilder {
        throw NotImplementedError()
    }

    override val user_auth_state: YoutubeApi.UserAuthState? = null
    override val UpdateUserAuthState = object : UserAuthStateEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun byHeaders(headers: Headers): Result<YoutubeApi.UserAuthState> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }

    override val YoutubeChannelCreationForm = object : YoutubeChannelCreationFormEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getForm(headers: Headers, channel_creation_token: String): Result<YoutubeAccountCreationForm.ChannelCreationForm> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val CreateYoutubeChannel = object : CreateYoutubeChannelEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun createYoutubeChannel(headers: Headers, channel_creation_token: String, params: Map<String, String>): Result<Artist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val LoadSong = object : LoadSongEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadSong(song_id: String): Result<Song> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val LoadArtist = object : LoadArtistEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadArtist(artist_id: String): Result<Artist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val LoadPlaylist = object : LoadPlaylistEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadPlaylist(playlist_id: String, continuation: RadioContinuation?, browse_params: String?, playlist_url: String?): Result<Playlist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val VideoFormats = object : VideoFormatsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getVideoFormats(id: String, filter: ((YoutubeVideoFormat) -> Boolean)?): Result<List<YoutubeVideoFormat>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val HomeFeed = object : HomeFeedEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getHomeFeed(min_rows: Int, allow_cached: Boolean, params: String?, continuation: String?): Result<HomeFeedLoadResult> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val GenericFeedViewMorePage = object : GenericFeedViewMorePageEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getGenericFeedViewMorePage(browse_id: String): Result<List<MediaItem>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val SongRadio = object : SongRadioEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongRadio(song_id: String, continuation: String?, filters: List<RadioBuilderModifier>): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val ArtistWithParams = object : ArtistWithParamsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadArtistWithParams(browse_params: YoutubePage.BrowseParamsData): Result<List<ArtistWithParamsRow>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val ArtistRadio: ArtistRadioEndpoint = object : ArtistRadioEndpoint() {
        override suspend fun getArtistRadio(artist_id: String, continuation: String?): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api: YoutubeApi = this@UnimplementedYoutubeApi
    }
    override val ArtistShuffle: ArtistShuffleEndpoint = object : ArtistShuffleEndpoint() {
        override suspend fun getArtistShuffle(artist_shuffle_playlist_id: String, continuation: String?): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api: YoutubeApi = this@UnimplementedYoutubeApi
    }
    override val PlaylistContinuation = object : PlaylistContinuationEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getPlaylistContinuation(initial: Boolean, token: String, skip_initial: Int): Result<Pair<List<MediaItem>, RadioContinuation?>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val Search = object : SearchEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun searchMusic(query: String, params: String?): Result<SearchResults> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val SearchSuggestions = object : SearchSuggestionsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSearchSuggestions(query: String): Result<List<SearchSuggestion>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val RadioBuilder = object : RadioBuilderEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getRadioBuilderArtists(selectThumbnail: (List<Thumbnail>) -> Thumbnail): Result<List<RadioBuilderArtist>> {
            throw NotImplementedError()
        }
        override fun buildRadioToken(artists: Set<RadioBuilderArtist>, modifiers: Set<RadioBuilderModifier?>): String {
            throw NotImplementedError()
        }
        override suspend fun getBuiltRadio(radio_token: String): Result<Playlist?> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val SongRelatedContent = object : SongRelatedContentEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongRelated(song_id: String): Result<List<RelatedGroup>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val SongLyrics = object : SongLyricsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongLyrics(lyrics_id: String): Result<String> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
}
