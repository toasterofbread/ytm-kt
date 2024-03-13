package dev.toastbits.ytmapi

import dev.toastbits.ytmapi.endpoint.*
import dev.toastbits.ytmapi.formats.VideoFormatsEndpoint
import dev.toastbits.ytmapi.impl.unimplemented.UnimplementedYoutubeApi
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.itemcache.MediaItemCache
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject

interface YoutubeApi {
    companion object {
        val INCLUDE_HEADERS: List<String> = listOf("cookie", "authorization", "x-goog-authuser")
    }

    val data_language: String
    val ui_language: String
    val item_cache: MediaItemCache?

    val client: HttpClient

    enum class Type {
        YOUTUBE_MUSIC,
        UNIMPLEMENTED_FOR_TESTING;

        fun isSelectable(): Boolean = this != UNIMPLEMENTED_FOR_TESTING

        fun getDefaultUrl(): String =
            when(this) {
                YOUTUBE_MUSIC -> "https://music.youtube.com/youtubei/v1/"
                UNIMPLEMENTED_FOR_TESTING -> ""
            }

        fun instantiate(api_url: String): YoutubeApi =
            when(this) {
                YOUTUBE_MUSIC -> YoutubeMusicApi(api_url)
                UNIMPLEMENTED_FOR_TESTING -> UnimplementedYoutubeApi()
            }

        companion object {
            val DEFAULT: Type = YOUTUBE_MUSIC
        }
    }

    suspend fun init()

    enum class PostBodyContext {
        BASE,
        ANDROID_MUSIC,
        ANDROID,
        MOBILE,
        UI_LANGUAGE
    }
    suspend fun PostBodyContext.getContextPostBody(): JsonObject

    fun HttpRequestBuilder.endpointPath(
        path: String
    ): HttpRequestBuilder

    suspend fun HttpRequestBuilder.addAuthlessApiHeaders(
        include: List<String>? = null
    ): HttpRequestBuilder

    suspend fun HttpRequestBuilder.addAuthApiHeaders(
        include: List<String>? = null
    ): HttpRequestBuilder {
        addAuthlessApiHeaders(include)
        user_auth_state?.addHeadersToRequest(this, include)
        return this
    }

    suspend fun HttpRequestBuilder.postWithBody(
        body: Map<String, Any?>? = null,
        context: PostBodyContext = PostBodyContext.BASE,
    ): HttpRequestBuilder

    interface Implementable {
        fun isImplemented(): Boolean = true

        fun getIdentifier(): String =
            this::class.simpleName ?: this::class.toString()

        fun getNotImplementedMessage(): String =
            "Implementable not implemented:\n${getIdentifier()}"

        fun getNotImplementedException(): NotImplementedError =
            NotImplementedError(getNotImplementedMessage())

        fun <T: Implementable> T.implementedOrNull(): T? =
            if (isImplemented()) this else null
    }

    abstract class Endpoint: Implementable {
        abstract val api: YoutubeApi
        override fun getNotImplementedMessage(): String = "Endpoint not implemented:\n${getIdentifier()}"

        fun HttpRequestBuilder.endpointPath(path: String): HttpRequestBuilder =
            with (api) {
                endpointPath(path)
            }

        open suspend fun HttpRequestBuilder.addAuthApiHeaders(include: List<String>? = null): HttpRequestBuilder =
            with (api) {
                addAuthApiHeaders(include)
            }

        open suspend fun HttpRequestBuilder.addApiHeadersNoAuth(include: List<String>? = null): HttpRequestBuilder =
            with (api) {
                addAuthlessApiHeaders(include)
            }

        suspend fun HttpRequestBuilder.postWithBody(body: Map<String, Any?>? = null, context: PostBodyContext = PostBodyContext.BASE): HttpRequestBuilder =
            with (api) {
                postWithBody(body, context)
            }
    }

    // -- User auth ---
    val user_auth_state: UserAuthState?
    val UpdateUserAuthState: UserAuthStateEndpoint
    val YoutubeChannelCreationForm: YoutubeChannelCreationFormEndpoint
    val CreateYoutubeChannel: CreateYoutubeChannelEndpoint

    // --- MediaItems ---
    val LoadSong: LoadSongEndpoint
    val LoadArtist: LoadArtistEndpoint
    val LoadPlaylist: LoadPlaylistEndpoint

    // --- Video formats ---
    val VideoFormats: VideoFormatsEndpoint

    // --- Feed ---
    val HomeFeed: HomeFeedEndpoint
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

    abstract class UserAuthState(
        headers: Map<String, String>
    ) {
        abstract val api: YoutubeApi
        abstract val own_channel_id: String?
        val headers: Headers

        init {
            val headers_builder: Headers.Builder = Headers.Builder()

            for (header in INCLUDE_HEADERS) {
                val value: String = headers[header] ?: continue
                if (header == "cookie") {
                    val filtered_cookies: String = filterCookieString(value) {
                        it.startsWith("__Secure-")
                    }

                    headers_builder.add("cookie", filtered_cookies)
                    continue
                }

                headers_builder.add(header, value)
            }

            this.headers = headers_builder        }

        private enum class ValueType { CHANNEL, HEADER }

        private fun filterCookieString(cookies_string: String, shouldKeepCookie: (String) -> Boolean): String {
            var ret: String = ""
            val cookies: List<String> = cookies_string.split(';')
                .mapNotNull {
                    it.trim().takeIf { it.isNotEmpty() }
                }

            for (cookie in cookies) {
                val (name, value) = cookie.split('=', limit = 2)

                if (!shouldKeepCookie(name)) {
                    continue
                }

                ret += "$name=$value;"
            }

            return ret
        }

        fun addHeadersToRequest(builder: HttpRequestBuilder, include: List<String>? = null) {
            builder.headers {
                if (!include.isNullOrEmpty()) {
                    for (header_key in include) {
                        val value = headers[header_key] ?: continue
                        set(header_key, value)
                    }
                }
                else {
                    for (header in headers) {
                        set(header.first, header.second)
                    }
                }
            }
        }

        abstract class UserAuthEndpoint: Endpoint() {
            protected abstract val auth: UserAuthState
            override val api: YoutubeApi get() = auth.api

            suspend fun HttpRequestBuilder.addAuthlessApiHeaders(include: List<String>?): HttpRequestBuilder =
                with (api) {
                    addAuthlessApiHeaders(include)
                }

            override suspend fun HttpRequestBuilder.addAuthApiHeaders(include: List<String>?): HttpRequestBuilder {
                addApiHeadersNoAuth()
                auth.addHeadersToRequest(this, include)
                return this
            }
        }

        // --- Account playlists ---
        abstract val AccountPlaylists: AccountPlaylistsEndpoint
        abstract val CreateAccountPlaylist: CreateAccountPlaylistEndpoint
        abstract val DeleteAccountPlaylist: DeleteAccountPlaylistEndpoint
        abstract val AccountPlaylistEditor: AccountPlaylistEditorEndpoint
        abstract val AccountPlaylistAddSongs: AccountPlaylistAddSongsEndpoint

        // --- Account liked items ---
        abstract val LikedAlbums: LikedAlbumsEndpoint
        abstract val LikedArtists: LikedArtistsEndpoint
        abstract val LikedPlaylists: LikedPlaylistsEndpoint

        // --- Interaction ---
        abstract val SubscribedToArtist: SubscribedToArtistEndpoint
        abstract val SetSubscribedToArtist: SetSubscribedToArtistEndpoint
        abstract val SongLiked: SongLikedEndpoint
        abstract val SetSongLiked: SetSongLikedEndpoint
        abstract val MarkSongAsWatched: MarkSongAsWatchedEndpoint
    }

    val HttpStatusCode.is_successful: Boolean get() = value in 200 .. 299
}
