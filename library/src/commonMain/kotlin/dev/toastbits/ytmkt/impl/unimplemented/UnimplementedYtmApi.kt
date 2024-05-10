package dev.toastbits.ytmkt.impl.unimplemented

import dev.toastbits.ytmkt.model.ApiAuthenticationState
import dev.toastbits.ytmkt.model.external.mediaitem.*
import dev.toastbits.ytmkt.endpoint.RadioBuilderArtist
import dev.toastbits.ytmkt.endpoint.RadioBuilderEndpoint
import dev.toastbits.ytmkt.endpoint.RadioBuilderModifier
import dev.toastbits.ytmkt.endpoint.SongRelatedContentEndpoint
import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.model.external.YoutubeVideoFormat
import dev.toastbits.ytmkt.model.external.YoutubePage
import dev.toastbits.ytmkt.model.external.Thumbnail
import dev.toastbits.ytmkt.endpoint.*
import dev.toastbits.ytmkt.formats.VideoFormatsEndpoint
import dev.toastbits.ytmkt.model.external.RelatedGroup
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import dev.toastbits.ytmkt.radio.RadioContinuation
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.Headers
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import dev.toastbits.ytmkt.model.external.YoutubeAccountCreationForm

open class UnimplementedYtmApi: YtmApi {
    override val client: HttpClient = HttpClient()
    override val json: Json = Json.Default
    override val item_cache: MediaItemCache = MediaItemCache()

    override fun HttpRequestBuilder.endpointPath(path: String, non_music_api: Boolean) {
        throw NotImplementedError()
    }

    override fun HttpRequestBuilder.addUnauthenticatedApiHeaders(include: List<String>?, non_music_api: Boolean) {
        throw NotImplementedError()
    }

    override fun HttpRequestBuilder.postWithBody(
        base: JsonObject?,
        buildPostBody: (JsonObjectBuilder.() -> Unit)?
    ) {
        throw NotImplementedError()
    }

    override val user_auth_state: ApiAuthenticationState? = null

    override val YoutubeChannelCreationForm = object : YoutubeChannelCreationFormEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getForm(headers: Headers, channel_creation_token: String): Result<YoutubeAccountCreationForm.ChannelCreationForm> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val CreateYoutubeChannel = object : CreateYoutubeChannelEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun createYoutubeChannel(headers: Headers, channel_creation_token: String, params: Map<String, String>): Result<YtmArtist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val LoadSong = object : LoadSongEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadSong(song_id: String): Result<YtmSong> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val LoadArtist = object : LoadArtistEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadArtist(artist_id: String): Result<YtmArtist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val LoadPlaylist = object : LoadPlaylistEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadPlaylist(playlist_id: String, continuation: RadioContinuation?, browse_params: String?, playlist_url: String?, use_non_music_api: Boolean): Result<YtmPlaylist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val VideoFormats = object : VideoFormatsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getVideoFormats(id: String, include_non_default: Boolean, filter: ((YoutubeVideoFormat) -> Boolean)?): Result<List<YoutubeVideoFormat>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val SongFeed = object : SongFeedEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongFeed(min_rows: Int, params: String?, continuation: String?): Result<SongFeedLoadResult> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val GenericFeedViewMorePage = object : GenericFeedViewMorePageEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getGenericFeedViewMorePage(browse_id: String): Result<List<YtmMediaItem>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val SongRadio = object : SongRadioEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongRadio(song_id: String, continuation: String?, filters: List<RadioBuilderModifier>): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val ArtistWithParams = object : ArtistWithParamsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadArtistWithParams(browse_params: YoutubePage.BrowseParamsData): Result<List<ArtistWithParamsRow>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val ArtistRadio: ArtistRadioEndpoint = object : ArtistRadioEndpoint() {
        override suspend fun getArtistRadio(artist_id: String, continuation: String?): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api: YtmApi = this@UnimplementedYtmApi
    }
    override val ArtistShuffle: ArtistShuffleEndpoint = object : ArtistShuffleEndpoint() {
        override suspend fun getArtistShuffle(artist_shuffle_playlist_id: String, continuation: String?): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api: YtmApi = this@UnimplementedYtmApi
    }
    override val PlaylistContinuation = object : PlaylistContinuationEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getPlaylistContinuation(initial: Boolean, token: String, skip_initial: Int): Result<Pair<List<YtmMediaItem>, RadioContinuation?>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val Search = object : SearchEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun searchMusic(query: String, params: String?): Result<SearchResults> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val SearchSuggestions = object : SearchSuggestionsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSearchSuggestions(query: String): Result<List<SearchSuggestion>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val RadioBuilder = object : RadioBuilderEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getRadioBuilderArtists(selectThumbnail: (List<Thumbnail>) -> Thumbnail): Result<List<RadioBuilderArtist>> {
            throw NotImplementedError()
        }
        override fun buildRadioToken(artists: Set<RadioBuilderArtist>, modifiers: Set<RadioBuilderModifier?>): String {
            throw NotImplementedError()
        }
        override suspend fun getBuiltRadio(radio_token: String): Result<YtmPlaylist?> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val SongRelatedContent = object : SongRelatedContentEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongRelated(song_id: String): Result<List<RelatedGroup>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
    override val SongLyrics = object : SongLyricsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getSongLyrics(lyrics_id: String): Result<String> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYtmApi
    }
}
