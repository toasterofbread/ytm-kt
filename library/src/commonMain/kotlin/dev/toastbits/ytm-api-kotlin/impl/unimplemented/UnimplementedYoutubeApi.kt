package dev.toastbits.ytmapi.impl.unimplemented

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.layout.BrowseParamsData
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import dev.toastbits.ytmapi.RadioBuilderArtist
import dev.toastbits.ytmapi.RadioBuilderEndpoint
import dev.toastbits.ytmapi.RadioBuilderModifier
import dev.toastbits.ytmapi.SongRelatedContentEndpoint
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.model.external.YoutubeVideoFormat
import dev.toastbits.ytmapi.endpoint.*
import dev.toastbits.ytmapi.formats.VideoFormatsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.RelatedGroup
import dev.toastbits.ytmapi.itemcache.MediaItemCache

open class UnimplementedYoutubeApi(
    override val data_language: String = "",
    override val ui_language: String = "",
    override val item_cache: MediaItemCache? = null
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

    override suspend fun HttpRequestBuilder.postWithBody(body: Map<String, Any?>?, context: YoutubeApi.PostBodyContext): HttpRequestBuilder {
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
        override suspend fun createYoutubeChannel(headers: Map<String, String>, channel_creation_token: String, params: Map<String, String>): Result<Artist> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val LoadSong = object : LoadSongEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadSong(song_id: String): Result<SongData> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val LoadArtist = object : LoadArtistEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadArtist(artist_id: String): Result<ArtistData> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val LoadPlaylist = object : LoadPlaylistEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadPlaylist(playlist_id: String, continuation: MediaItemLayout.Continuation?, browse_params: String?, playlist_url: String?): Result<Playlist> {
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
        override suspend fun getSongRadio(video_id: String, continuation: String?, filters: List<RadioBuilderModifier>): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val ArtistWithParams = object : ArtistWithParamsEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun loadArtistWithParams(browse_params: BrowseParamsData): Result<List<ArtistWithParamsRow>> {
            throw NotImplementedError()
        }
        override val api = this@UnimplementedYoutubeApi
    }
    override val ArtistRadio: ArtistRadioEndpoint = object : ArtistRadioEndpoint() {
        override suspend fun getArtistRadio(artist: Artist, continuation: String?): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api: YoutubeApi = this@UnimplementedYoutubeApi
    }
    override val ArtistShuffle: ArtistShuffleEndpoint = object : ArtistShuffleEndpoint() {
        override suspend fun getArtistShuffle(artist: Artist, continuation: String?): Result<RadioData> {
            throw NotImplementedError()
        }
        override val api: YoutubeApi = this@UnimplementedYoutubeApi
    }
    override val PlaylistContinuation = object : PlaylistContinuationEndpoint() {
        override fun isImplemented(): Boolean = false
        override suspend fun getPlaylistContinuation(initial: Boolean, token: String, skip_initial: Int): Result<Pair<List<MediaItemData>, String?>> {
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
