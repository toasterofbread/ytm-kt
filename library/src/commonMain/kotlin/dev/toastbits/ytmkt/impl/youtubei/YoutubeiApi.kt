package dev.toastbits.ytmkt.impl.youtubei

import dev.toastbits.ytmkt.model.YtmApi
import dev.toastbits.ytmkt.endpoint.ArtistRadioEndpoint
import dev.toastbits.ytmkt.endpoint.ArtistShuffleEndpoint
import dev.toastbits.ytmkt.formats.VideoFormatsEndpoint
import dev.toastbits.ytmkt.formats.YoutubeiVideoFormatsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.endpoint.*
import dev.toastbits.ytmkt.itemcache.MediaItemCache
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject

/**
 * Implementation of [YtmApi] which directly accesses YouTube Music using the youtubei/v1 API.
 * See endpoint class definitions for usage documentation.
 *
 * @property data_language Two-part language code for data strings such as song and artist names.
 * @property api_url Base API url to use for requests. Defaults to https://music.youtube.com/youtubei/v1/.
 */
@Suppress("LeakingThis")
open class YoutubeiApi(
    open val data_language: String = "en-GB",
    val api_url: String = DEFAULT_API_URL,
    val non_music_api_url: String = DEFAULT_NON_MUSIC_API_URL,
    override val item_cache: MediaItemCache = MediaItemCache()
): YtmApi {
    override var user_auth_state: YoutubeiAuthenticationState? = null
    var visitor_id: String? = null

    companion object {
        const val DEFAULT_API_URL: String = "https://music.youtube.com/youtubei/v1/"
        const val DEFAULT_NON_MUSIC_API_URL: String = "https://www.youtube.com/youtubei/v1/"
    }

    // -- User auth ---
    override val YoutubeChannelCreationForm = YTMYoutubeChannelCreationFormEndpoint(this)
    override val CreateYoutubeChannel = YTMCreateYoutubeChannelEndpoint(this)
    val GetVisitorId = YTMGetVisitorIdEndpoint(this)

    // --- Item loading ---
    override val LoadSong = YTMLoadSongEndpoint(this)
    override val LoadArtist = YTMLoadArtistEndpoint(this)
    override val LoadPlaylist = YTMLoadPlaylistEndpoint(this)

    // --- Video formats ---
    override val VideoFormats: VideoFormatsEndpoint = YoutubeiVideoFormatsEndpoint(this)

    // --- Feed ---
    override val SongFeed = YTMGetSongFeedEndpoint(this)
    override val GenericFeedViewMorePage = YTMGenericFeedViewMorePageEndpoint(this)
    override val SongRadio = YTMSongRadioEndpoint(this)

    // --- Artists ---
    override val ArtistWithParams = YTMArtistWithParamsEndpoint(this)
    override val ArtistRadio: ArtistRadioEndpoint = YTMArtistRadioEndpoint(this)
    override val ArtistShuffle: ArtistShuffleEndpoint = YTMArtistShuffleEndpoint(this)

    // --- Playlists ---
    override val PlaylistContinuation = YTMPlaylistContinuationEndpoint(this)

    // --- Search ---
    override val Search = YTMSearchEndpoint(this)
    override val SearchSuggestions = YTMSearchSuggestionsEndpoint(this)

    // --- Radio builder ---
    override val RadioBuilder = YTMRadioBuilderEndpoint(this)

    // --- Song content ---
    override val SongRelatedContent = YTMSongRelatedContentEndpoint(this)
    override val SongLyrics = YTMSongLyricsEndpoint(this)

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override val json: Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    override val client: HttpClient = HttpClient() {
        configureClient()
    }

    /**
     * Configures the default HttpClient used by endpoints.
     */
    @OptIn(ExperimentalSerializationApi::class)
    protected open fun HttpClientConfig<out HttpClientEngineConfig>.configureClient() {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
    }

    override fun HttpRequestBuilder.endpointPath(path: String, non_music_api: Boolean) {
        url.takeFrom((if (!non_music_api) api_url else non_music_api_url).removeSuffix("/"))
        url.pathSegments += path.split("/")
        url.parameters.append("prettyPrint", "false")
    }

    override fun HttpRequestBuilder.postWithBody(
        base: JsonObject?,
        buildPostBody: (JsonObjectBuilder.() -> Unit)?
    ) {
        method = HttpMethod.Post
        contentType(ContentType.Application.Json)

        val base_body: JsonObject = base ?: YoutubeiPostBody.DEFAULT.getPostBody(this@YoutubeiApi)

        if (buildPostBody == null) {
            setBody(Json.encodeToString(base_body))
        }
        else {
            val body: MutableMap<String, JsonElement> = buildJsonObject(buildPostBody).toMutableMap()
            for ((key, value) in base_body) {
                check(!body.containsKey(key)) {
                    "Post body from endpoint contains key $key which conflicts with the base body"
                }

                body[key] = value
            }
            setBody(Json.encodeToString(body))
        }
    }

    override fun HttpRequestBuilder.addUnauthenticatedApiHeaders(include: List<String>?, non_music_api: Boolean) {
        addUnauthenticatedApiHeaders(include, add_visitor_id = true, non_music_api = non_music_api)
    }

    fun HttpRequestBuilder.addUnauthenticatedApiHeaders(
        include: List<String>? = null,
        add_visitor_id: Boolean = true,
        non_music_api: Boolean = false
    ) {
        headers {
            val post_headers: Headers = if (non_music_api) non_music_post_headers else music_post_headers

            if (!include.isNullOrEmpty()) {
                for (header_key in include) {
                    val value: String = post_headers[header_key] ?: continue
                    set(header_key, value)
                }
            }
            else {
                for ((key, value) in post_headers.entries()) {
                    set(key, value.first())
                }
            }

            if (add_visitor_id) {
                visitor_id?.also {
                    set("X-Goog-EOM-Visitor-Id", it)
                }
            }
        }
    }

    private val music_post_headers: Headers by lazy {
        Headers.build {
            for ((key, value) in YoutubeiRequestData.getYtmHeaders(api_url)) {
                append(key, value)
            }
            set("origin", api_url)
        }
    }
    private val non_music_post_headers: Headers by lazy {
        Headers.build {
            for ((key, value) in YoutubeiRequestData.getYtmHeaders(non_music_api_url)) {
                append(key, value)
            }
            set("origin", non_music_api_url)
        }
    }

    private val data_hl: String get() = data_language.split('-', limit = 2).first()

    internal val post_body_default: JsonObject get() = YoutubeiRequestData.getYtmContext(data_hl)
    internal val post_body_android_music: JsonObject get() = YoutubeiRequestData.getYtmContextAndroidMusic(data_hl)
    internal val post_body_android: JsonObject get() = YoutubeiRequestData.getYtmContextAndroid(data_hl)
    internal val post_body_mobile: JsonObject get() = YoutubeiRequestData.getYtmContextMobile(data_hl)
    internal val post_body_web: JsonObject get() = YoutubeiRequestData.getYtmContextWeb(data_hl)
}
